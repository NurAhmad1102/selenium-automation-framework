package com.automation.framework.core.driver;

import com.automation.framework.core.config.FrameworkConfig;

/**
 * Agnostic developer mode utilities for attaching to existing running browser sessions.
 */
public final class DevModeUtils {

    private DevModeUtils() {
    }

    public static boolean isDevMode() {
        return FrameworkConfig.isDevMode();
    }

    public static String getDevPort() {
        return FrameworkConfig.getDevPort();
    }
}
