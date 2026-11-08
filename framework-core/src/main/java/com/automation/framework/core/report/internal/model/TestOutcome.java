package com.automation.framework.core.report.internal.model;

import com.aventstack.extentreports.Status;

public class TestOutcome {
    private final OutcomeStatus status;
    private final OutcomeCategory category;
    private final String reason;

    public TestOutcome(OutcomeStatus status, OutcomeCategory category, String reason) {
        this.status = status != null ? status : OutcomeStatus.PASS;
        this.category = category != null ? category : OutcomeCategory.LIFECYCLE;
        this.reason = reason != null && !reason.isBlank() ? reason : "No reason provided";
    }

    public OutcomeStatus getStatus() {
        return status;
    }

    public OutcomeCategory getCategory() {
        return category;
    }

    public String getReason() {
        return reason;
    }

    public Status getExtentStatus() {
        return status.getExtentStatus();
    }
}
