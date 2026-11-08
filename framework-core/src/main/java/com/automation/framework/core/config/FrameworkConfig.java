package com.automation.framework.core.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Centralized, agnostic configuration manager for the automation framework.
 * Resolves properties following a priority cascade:
 * 1. Java System Properties (-Dproperty=value)
 * 2. Operating System Environment Variables
 * 3. Root .env file
 * 4. Classpath application-{env}.properties (if env specified)
 * 5. Classpath application.properties / framework.properties
 * 6. Built-in defaults
 */
public final class FrameworkConfig {

    private static final Logger logger = LogManager.getLogger(FrameworkConfig.class);
    private static final Properties properties = new Properties();
    private static final Properties envFileProperties = new Properties();
    private static boolean initialized = false;

    private FrameworkConfig() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        loadEnvFile();
        loadPropertiesFiles();
    }

    private static void loadEnvFile() {
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
                    envFileProperties.put(key, value);
                }
                logger.info("Loaded .env file configurations from: {}", envPath);
            } catch (Exception e) {
                logger.warn("Failed to read .env file from {}: {}", envPath, e.getMessage());
            }
        }
    }

    private static void loadPropertiesFiles() {
        // Load default framework.properties first
        loadClasspathProperties("framework.properties");

        // Load application.properties
        loadClasspathProperties("application.properties");

        // Check active environment without causing recursive init
        String env = resolveRawProperty("env");
        if (env == null) {
            env = resolveRawProperty("automation.env");
        }
        if (env == null) {
            env = "local";
        }
        env = env.toLowerCase().trim();

        if (!env.equals("local")) {
            loadClasspathProperties("application-" + env + ".properties");
        }
    }

    private static void loadClasspathProperties(String resourceName) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourceName)) {
            if (is != null) {
                Properties p = new Properties();
                p.load(is);
                properties.putAll(p);
                logger.info("Loaded classpath configuration: {}", resourceName);
            }
        } catch (Exception e) {
            logger.warn("Could not load classpath properties {}: {}", resourceName, e.getMessage());
        }
    }

    private static String resolveRawProperty(String key) {
        String sys = System.getProperty(key);
        if (sys != null && !sys.isBlank()) return sys.trim();

        String env = System.getenv(key);
        if (env != null && !env.isBlank()) return env.trim();

        String fileVal = envFileProperties.getProperty(key);
        if (fileVal != null && !fileVal.isBlank()) return fileVal.trim();

        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.isBlank()) return propVal.trim();

        return null;
    }

    /**
     * Get a property by key adhering to priority resolution.
     */
    public static String get(String key) {
        init();
        // 1. System Property
        String sysProp = System.getProperty(key);
        if (sysProp != null && !sysProp.isBlank()) {
            return sysProp.trim();
        }

        // Alternative key with dot replaced by underscore or uppercase
        String envVarKey = key.replace('.', '_').toUpperCase();
        String sysPropAlt = System.getProperty(envVarKey);
        if (sysPropAlt != null && !sysPropAlt.isBlank()) {
            return sysPropAlt.trim();
        }

        // 2. OS Environment Variable
        String envVal = System.getenv(key);
        if (envVal != null && !envVal.isBlank()) {
            return envVal.trim();
        }
        String envValAlt = System.getenv(envVarKey);
        if (envValAlt != null && !envValAlt.isBlank()) {
            return envValAlt.trim();
        }

        // 3. .env file
        String envFileVal = envFileProperties.getProperty(key);
        if (envFileVal == null) {
            envFileVal = envFileProperties.getProperty(envVarKey);
        }
        if (envFileVal != null && !envFileVal.isBlank()) {
            return envFileVal.trim();
        }

        // 4. Loaded properties files
        String propVal = properties.getProperty(key);
        if (propVal != null && !propVal.isBlank()) {
            return propVal.trim();
        }

        return null;
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val.trim());
    }

    public static int getInt(String key, int defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(val.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid integer for configuration key {}: {}. Using default {}", key, val, defaultValue);
            return defaultValue;
        }
    }

    public static long getLong(String key, long defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (NumberFormatException e) {
            logger.warn("Invalid long for configuration key {}: {}. Using default {}", key, val, defaultValue);
            return defaultValue;
        }
    }

    // Helper shortcuts
    public static String getBrowser() {
        return get("browser", "chrome").toLowerCase();
    }

    public static boolean isHeadless() {
        return getBoolean("headless", false);
    }

    public static int getDefaultTimeout() {
        return getInt("timeout.default", 15);
    }

    public static int getPageLoadTimeout() {
        return getInt("timeout.pageload", 300);
    }

    public static long getAccountWaitTimeoutMs() {
        return getLong("account.wait.timeout.seconds", 30) * 1000;
    }

    public static String getBaseUrl() {
        return get("base.url", "");
    }

    public static boolean isDevMode() {
        return getBoolean("devMode", getBoolean("dev.mode", false));
    }

    public static String getDevPort() {
        return get("devPort", get("dev.port", "9222"));
    }

    public static String getReportDirectory() {
        return get("report.dir", "reports");
    }

    public static String getScreenshotDirectory() {
        return get("screenshot.dir", "reports/capture");
    }
}
