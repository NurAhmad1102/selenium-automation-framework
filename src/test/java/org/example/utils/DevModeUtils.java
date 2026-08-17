package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Utility to manage Developer Mode (devMode).
 * Priority order for configuration:
 * 1. System Properties (-DdevMode=true or -DdevPort=9222)
 * 2. OS Environment Variables (DEV_MODE=true or DEV_PORT=9222)
 * 3. Root .env file (DEV_MODE=true or DEV_PORT=9222)
 */
public class DevModeUtils {
    private static final Logger logger = LogManager.getLogger(DevModeUtils.class);
    private static final Properties envProps = new Properties();
    private static boolean envLoaded = false;

    private static synchronized void loadEnvFile() {
        if (envLoaded) return;
        envLoaded = true;
        Path envPath = Paths.get(System.getProperty("user.dir"), ".env");
        if (Files.exists(envPath)) {
            try (BufferedReader reader = Files.newBufferedReader(envPath)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                        continue;
                    }
                    int eqIdx = line.indexOf('=');
                    String key = line.substring(0, eqIdx).trim();
                    String value = line.substring(eqIdx + 1).trim();
                    if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }
                    envProps.put(key, value);
                }
                logger.info("[DEV-MODE] Loaded environment configuration from .env file.");
            } catch (Exception e) {
                logger.warn("Failed to load .env file from {}", envPath, e);
            }
        }
    }

    public static boolean isDevMode() {
        // Priority 1: System Property
        String devModeProp = System.getProperty("devMode");
        if (devModeProp == null) {
            devModeProp = System.getProperty("dev.mode");
        }
        if (devModeProp != null) {
            return Boolean.parseBoolean(devModeProp.trim());
        }

        // Priority 2: OS Environment Variable
        String envVar = System.getenv("DEV_MODE");
        if (envVar == null) {
            envVar = System.getenv("devMode");
        }
        if (envVar != null) {
            return Boolean.parseBoolean(envVar.trim());
        }

        // Priority 3: .env file
        loadEnvFile();
        String fileVal = envProps.getProperty("DEV_MODE");
        if (fileVal == null) {
            fileVal = envProps.getProperty("devMode");
        }
        if (fileVal != null) {
            return Boolean.parseBoolean(fileVal.trim());
        }

        return false;
    }

    public static String getDevPort() {
        // Priority 1: System Property
        String port = System.getProperty("devPort");
        if (port == null || port.isBlank()) {
            port = System.getProperty("dev.port");
        }
        if (port != null && !port.isBlank()) {
            return port.trim();
        }

        // Priority 2: OS Environment Variable
        String envPort = System.getenv("DEV_PORT");
        if (envPort == null || envPort.isBlank()) {
            envPort = System.getenv("devPort");
        }
        if (envPort != null && !envPort.isBlank()) {
            return envPort.trim();
        }

        // Priority 3: .env file
        loadEnvFile();
        String filePort = envProps.getProperty("DEV_PORT");
        if (filePort == null || filePort.isBlank()) {
            filePort = envProps.getProperty("devPort");
        }
        if (filePort != null && !filePort.isBlank()) {
            return filePort.trim();
        }

        return "9222";
    }
}
