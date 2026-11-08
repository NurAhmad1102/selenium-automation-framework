package com.automation.framework.core.driver;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;

/**
 * Thread-safe WebDriver storage via ThreadLocal.
 */
public final class DriverManager {

    private static final Logger logger = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    private DriverManager() {
    }

    public static void setDriver(WebDriver webDriver) {
        driver.set(webDriver);
    }

    public static WebDriver getDriver() {
        return driver.get();
    }

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
