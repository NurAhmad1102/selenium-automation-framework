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

    public static void cleanupCurrentJvmDescendants() {
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
