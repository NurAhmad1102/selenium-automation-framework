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

    /**
     * Retrieves the string configuration value for a key, or defaultValue if not found.
     *
     * @param key configuration property key
     * @param defaultValue default value if key is not configured
     * @return configured string value or defaultValue
     */
    public static String get(String key, String defaultValue) {
        String val = get(key);
        return (val != null && !val.isBlank()) ? val : defaultValue;
    }

    /**
     * Retrieves a boolean configuration value.
     *
     * @param key configuration property key
     * @param defaultValue default boolean value if key is not configured
     * @return boolean value
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String val = get(key);
        if (val == null) {
            return defaultValue;
        }
        return Boolean.parseBoolean(val.trim());
    }

    /**
     * Retrieves an integer configuration value.
     *
     * @param key configuration property key
     * @param defaultValue default integer value if key is not configured or invalid
     * @return integer value
     */
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

    /**
     * Retrieves a long integer configuration value.
     *
     * @param key configuration property key
     * @param defaultValue default long value if key is not configured or invalid
     * @return long value
     */
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

    /**
     * Returns the target browser name (e.g., "chrome", "firefox", "edge").
     * Default: "chrome".
     *
     * @return lowercase browser name
     */
    public static String getBrowser() {
        return get("browser", "chrome").toLowerCase();
    }

    /**
     * Indicates whether headless browser mode is enabled.
     * Default: false.
     *
     * @return true if headless execution is requested
     */
    public static boolean isHeadless() {
        return getBoolean("headless", false);
    }

    /**
     * Returns the default explicit wait timeout in seconds.
     * Default: 15 seconds.
     *
     * @return default timeout in seconds
     */
    public static int getDefaultTimeout() {
        return getInt("timeout.default", 15);
    }

    /**
     * Returns the page load timeout in seconds.
     * Default: 300 seconds.
     *
     * @return page load timeout in seconds
     */
    public static int getPageLoadTimeout() {
        return getInt("timeout.pageload", 300);
    }

    /**
     * Returns the maximum time to wait for a shared user account to be released, in milliseconds.
     * Default: 30,000 ms (30 seconds).
     *
     * @return wait timeout in milliseconds
     */
    public static long getAccountWaitTimeoutMs() {
        return getLong("account.wait.timeout.seconds", 30) * 1000;
    }

    /**
     * Returns the configured base URL for the web application under test.
     *
     * @return base URL string
     */
    public static String getBaseUrl() {
        return get("base.url", "");
    }

    /**
     * Checks if developer mode (attaching to an existing browser) is enabled.
     *
     * @return true if devMode is enabled
     */
    public static boolean isDevMode() {
        return getBoolean("devMode", getBoolean("dev.mode", false));
    }

    /**
     * Returns the developer mode remote debugging port.
     * Default: "9222".
     *
     * @return remote debugging port string
     */
    public static String getDevPort() {
        return get("devPort", get("dev.port", "9222"));
    }

    /**
     * Returns the target directory for generated HTML reports.
     * Default: "reports".
     *
     * @return report directory path string
     */
    public static String getReportDirectory() {
        return get("report.dir", "reports");
    }

    /**
     * Returns the target directory for captured test failure screenshots.
     * Default: "reports/capture".
     *
     * @return screenshot directory path string
     */
    public static String getScreenshotDirectory() {
        return get("screenshot.dir", "reports/capture");
    }
}
