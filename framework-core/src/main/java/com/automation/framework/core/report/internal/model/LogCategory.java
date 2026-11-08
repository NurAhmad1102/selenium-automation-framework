package com.automation.framework.core.report.internal.model;

public enum LogCategory {
    DETAILS("Details"),
    ELEMENT_LOGS("Element Logs"),
    NETWORK_LOGS("Network Logs");

    private final String title;

    LogCategory(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
