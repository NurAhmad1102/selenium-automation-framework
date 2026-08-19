package org.example.drivers;

import org.openqa.selenium.WebDriver;

import org.example.utils.DevModeUtils;

public class DriverManager {
    private static final org.apache.logging.log4j.Logger logger = 
        org.apache.logging.log4j.LogManager.getLogger(DriverManager.class);

    private static ThreadLocal<WebDriver> driver = new ThreadLocal<>();

    public static void setDriver(WebDriver webDriver){
        driver.set(webDriver);
    }

    public static WebDriver getDriver(){
        return driver.get();
    }

    public static void quitDriver() {
        WebDriver currentDriver = driver.get();
        if (currentDriver == null) {
            driver.remove();
            return;
        }
        if (DevModeUtils.isDevMode()) {
            logger.info("[DEV-MODE] Skipping WebDriver.quit() to keep browser open for development.");
            driver.remove();
            return;
        }
        try {
            currentDriver.quit();
        } catch (Exception e) {
            logger.error("Failed to quit WebDriver cleanly: {} - {}", 
                e.getClass().getSimpleName(), e.getMessage(), e);
        } finally {
            driver.remove();
        }
    }

}
