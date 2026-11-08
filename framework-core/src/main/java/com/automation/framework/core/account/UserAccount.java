package com.automation.framework.core.account;

/**
 * Agnostic representation of a test user account.
 */
public interface UserAccount {

    String getUsername();

    String getPassword();

    String getRole();

    default String getIdentifier() {
        return getUsername();
    }
}
