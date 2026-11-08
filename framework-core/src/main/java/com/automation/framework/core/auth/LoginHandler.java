package com.automation.framework.core.auth;

import com.automation.framework.core.account.UserAccount;
import org.openqa.selenium.WebDriver;

/**
 * Pluggable login and session management interface for target web applications.
 */
public interface LoginHandler {

    /**
     * Executes login steps with the specified account.
     */
    void login(WebDriver driver, UserAccount account);

    /**
     * Verifies if the browser session is currently authenticated.
     */
    boolean isLoggedIn(WebDriver driver);

    /**
     * Executes logout procedure if needed.
     */
    void logout(WebDriver driver);
}
