package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public final class BrowserProcessCleaner {

    private static final Logger logger = LogManager.getLogger(BrowserProcessCleaner.class);

    private static final Set<String> BROWSER_NAMES = Set.of(
            "chrome.exe", "chrome",
            "msedge.exe", "msedge",
            "firefox.exe", "firefox"
    );

    private static final Set<String> DRIVER_NAMES = Set.of(
            "chromedriver.exe", "chromedriver",
            "msedgedriver.exe", "msedgedriver",
            "geckodriver.exe", "geckodriver"
    );

    private static final java.util.concurrent.atomic.AtomicBoolean hookRegistered = new java.util.concurrent.atomic.AtomicBoolean(false);

    public static void registerShutdownHook() {
        if (hookRegistered.compareAndSet(false, true)) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (!DevModeUtils.isDevMode()) {
                    cleanupCurrentJvmDescendants();
                }
            }, "BrowserProcessCleaner-ShutdownHook"));
        }
    }

    public static void cleanupCurrentJvmDescendants() {
        if (DevModeUtils.isDevMode()) {
            logger.info("[DEV-MODE] Skipping JVM descendant process cleanup sweeper.");
            return;
        }
        try {
            logger.info("Starting JVM-local descendant process cleanup sweeper...");
            
            List<ProcessHandle> descendants = ProcessHandle.current()
                    .descendants()
                    .collect(Collectors.toList());

            // Terminate browsers first
            descendants.stream()
                    .filter(p -> isTargetProcess(p, BROWSER_NAMES))
                    .forEach(BrowserProcessCleaner::terminate);

            // Terminate drivers second
            descendants.stream()
                    .filter(p -> isTargetProcess(p, DRIVER_NAMES))
                    .forEach(BrowserProcessCleaner::terminate);

        } catch (Exception e) {
            logger.error("Exception during JVM-local process cleanup: {}", e.getMessage(), e);
        }
    }

    public static void cleanupOldBrowserProfiles() {
        if (DevModeUtils.isDevMode()) {
            return;
        }
        try {
            String root = System.getenv("SELENIUM_PROFILE_ROOT");
            if (root == null || root.isBlank()) {
                root = java.nio.file.Paths.get(System.getProperty("user.dir"), ".selenium").toString();
            }
            String buildTag = System.getenv("BUILD_TAG");
            if (buildTag == null || buildTag.isBlank()) {
                buildTag = "local";
            }
            buildTag = buildTag.replaceAll("[^a-zA-Z0-9._-]", "_");

            java.nio.file.Path targetDir = java.nio.file.Paths.get(root, buildTag);
            if (!java.nio.file.Files.exists(targetDir) || !java.nio.file.Files.isDirectory(targetDir)) {
                return;
            }

            long cutoffTime = System.currentTimeMillis() - java.time.Duration.ofHours(24).toMillis();

            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(targetDir)) {
                stream.filter(java.nio.file.Files::isDirectory)
                        .filter(dir -> {
                            try {
                                return java.nio.file.Files.getLastModifiedTime(dir).toMillis() < cutoffTime;
                            } catch (Exception e) {
                                return false;
                            }
                        })
                        .forEach(dir -> {
                            try {
                                org.apache.commons.io.FileUtils.deleteQuietly(dir.toFile());
                            } catch (Exception e) {
                                logger.debug("Could not delete stale profile folder: {}", dir, e);
                            }
                        });
            }
        } catch (Exception e) {
            logger.warn("Exception during stale browser profile cleanup: {}", e.getMessage());
        }
    }

    private static boolean isTargetProcess(ProcessHandle process, Set<String> targetNames) {
        if (!process.isAlive()) {
            return false;
        }

        String execName = resolveExecutableName(process);
        if (execName.isBlank()) {
            return false;
        }

        return targetNames.contains(execName);
    }

    private static String resolveExecutableName(ProcessHandle process) {
        String command = process.info().command().orElse("");
        if (!command.isBlank()) {
            return Paths.get(command).getFileName().toString().toLowerCase();
        }

        String commandLine = process.info().commandLine().orElse("");
        if (commandLine.isBlank()) {
            return "";
        }

        String executable;
        if (commandLine.startsWith("\"")) {
            int endQuote = commandLine.indexOf("\"", 1);
            if (endQuote <= 1) {
                return "";
            }
            executable = commandLine.substring(1, endQuote);
        } else {
            int firstSpace = commandLine.indexOf(" ");
            executable = firstSpace > 0 ? commandLine.substring(0, firstSpace) : commandLine;
        }

        try {
            return Paths.get(executable).getFileName().toString().toLowerCase();
        } catch (Exception e) {
            return "";
        }
    }

    private static void terminate(ProcessHandle process) {
        if (!process.isAlive()) {
            return;
        }

        long pid = process.pid();
        String command = process.info().command().orElse("");
        if (command.isBlank()) {
            command = process.info().commandLine().orElse("unknown");
        }

        try {
            logger.warn("Terminating leftover process: {} (PID: {})", command, pid);
            process.destroy();

            boolean exited = process.onExit()
                    .completeOnTimeout(null, 3, TimeUnit.SECONDS)
                    .join() != null;

            if (!exited && process.isAlive()) {
                logger.warn("Process did not exit in 3s, forcing destruction of PID {}", pid);
                process.destroyForcibly();
            }
        } catch (Exception e) {
            logger.error("Failed to terminate process PID {}: {}", pid, e.getMessage());
        }
    }
}
