package com.automation.framework.core.driver;

import com.automation.framework.core.config.FrameworkConfig;

/**
 * Agnostic developer mode utilities for attaching to existing running browser sessions.
 * Allows developers to attach tests directly to a Chrome instance started with
 * remote debugging enabled (--remote-debugging-port), skipping browser launch and login.
 */
public final class DevModeUtils {

    private DevModeUtils() {
    }

    /**
     * Checks if developer mode is enabled via configuration (devMode=true).
     *
     * @return true if developer mode is active
     */
    public static boolean isDevMode() {
        return FrameworkConfig.isDevMode();
    }

    /**
     * Retrieves the configured remote debugging port.
     *
     * @return remote debugging port (default: "9222")
     */
    public static String getDevPort() {
        return FrameworkConfig.getDevPort();
    }
}
