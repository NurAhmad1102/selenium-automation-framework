package com.automation.framework.core.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Thread-safe WebDriver storage via ThreadLocal.
 * Manages the lifecycle of WebDriver instances for each test thread, ensuring
 * parallel test executions do not interfere with each other.
 */
public final class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverManager() {
    }

    /**
     * Binds a WebDriver instance to the current execution thread.
     *
     * @param webDriver active WebDriver instance
     */
    public static void setDriver(WebDriver webDriver) {
        driver.set(webDriver);
    }

    /**
     * Retrieves the WebDriver instance associated with the current execution thread.
     *
     * @return current thread's WebDriver, or null if none is bound
     */
    public static WebDriver getDriver() {
        return driver.get();
    }

    /**
     * Quits and cleans up the WebDriver instance for the current execution thread.
     * In DevMode (devMode=true), the browser session is kept open for rapid inspection.
     */
    public static void quitDriver() {
        WebDriver currentDriver = driver.get();
        if (currentDriver == null) {
            driver.remove();
            return;
        }

        if (DevModeUtils.isDevMode()) {
            logger.info("[DEV-MODE] Skipping WebDriver.quit() to keep browser session open for development.");
            driver.remove();
            return;
        }

        try {
            currentDriver.quit();
            logger.info("WebDriver closed successfully for current thread.");
        } catch (Exception e) {
            logger.error("Failed to quit WebDriver cleanly: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
        } finally {
            driver.remove();
        }
    }
}
