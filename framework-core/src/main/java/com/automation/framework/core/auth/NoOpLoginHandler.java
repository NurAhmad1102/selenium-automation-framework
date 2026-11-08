package com.automation.framework.core.auth;

import com.automation.framework.core.account.UserAccount;
import org.openqa.selenium.WebDriver;

/**
 * Default no-op login handler for applications or tests that do not require authentication.
 */
public class NoOpLoginHandler implements LoginHandler {

    @Override
    public void login(WebDriver driver, UserAccount account) {
        // No-op
    }

    @Override
    public boolean isLoggedIn(WebDriver driver) {
        return true;
    }

    @Override
    public void logout(WebDriver driver) {
        // No-op
    }
}
