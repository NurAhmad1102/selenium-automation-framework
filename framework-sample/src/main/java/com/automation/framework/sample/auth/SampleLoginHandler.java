package com.automation.framework.sample.auth;

import com.automation.framework.core.account.UserAccount;
import com.automation.framework.core.auth.LoginHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

/**
 * Sample implementation of LoginHandler showcasing how to adapt
 * login authentication for any target web application.
 */
public class SampleLoginHandler implements LoginHandler {

    private static final Logger logger = LogManager.getLogger(SampleLoginHandler.class);

    @Override
    public void login(WebDriver driver, UserAccount account) {
        logger.info("Executing sample login for user: {}", account.getUsername());
        try {
            if (driver.findElements(By.id("username")).size() > 0) {
                driver.findElement(By.id("username")).sendKeys(account.getUsername());
                driver.findElement(By.id("password")).sendKeys(account.getPassword());
                driver.findElement(By.id("login-btn")).click();
            }
        } catch (Exception e) {
            logger.warn("Sample login element not encountered or skipped: {}", e.getMessage());
        }
    }

    @Override
    public boolean isLoggedIn(WebDriver driver) {
        return driver.findElements(By.id("logout-btn")).size() > 0;
    }

    @Override
    public void logout(WebDriver driver) {
        if (isLoggedIn(driver)) {
            driver.findElement(By.id("logout-btn")).click();
        }
    }
}
