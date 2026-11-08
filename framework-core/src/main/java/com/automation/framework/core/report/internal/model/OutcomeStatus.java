package com.automation.framework.core.report.internal.model;

import com.aventstack.extentreports.Status;

public enum OutcomeStatus {
    PASS(0, Status.PASS),
    INFO(0, Status.INFO),
    WARNING(1, Status.INFO),
    SKIP(2, Status.SKIP),
    BLOCKED(3, Status.SKIP),
    FAIL(4, Status.FAIL),
    BROKEN(5, Status.FAIL);

    private final int severity;
    private final Status extentStatus;

    OutcomeStatus(int severity, Status extentStatus) {
        this.severity = severity;
        this.extentStatus = extentStatus;
    }

    public int getSeverity() {
        return severity;
    }

    public Status getExtentStatus() {
        return extentStatus;
    }

    public boolean isMoreSevereThan(OutcomeStatus other) {
        return other == null || severity > other.severity;
    }
}
