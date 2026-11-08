package com.automation.framework.core.report.core;

import com.automation.framework.core.report.internal.model.LogEntry;
import com.automation.framework.core.report.internal.model.OutcomeCategory;
import com.automation.framework.core.report.internal.model.OutcomeStatus;
import com.automation.framework.core.report.internal.model.ReportData;
import com.automation.framework.core.report.internal.model.TestOutcome;
import com.aventstack.extentreports.Status;

import java.util.Comparator;
import java.util.List;

public final class TestOutcomeResolver {

    private TestOutcomeResolver() {
    }

    public static TestOutcome resolve(ReportData data) {
        if (data == null) {
            return passOutcome();
        }
        return resolve(data.getLogs());
    }

    public static TestOutcome resolve(List<LogEntry> logs) {
        if (logs == null || logs.isEmpty()) {
            return passOutcome();
        }

        return logs.stream()
                .filter(LogEntry::isAffectsFinalOutcome)
                .max(Comparator
                        .comparing((LogEntry log) -> normalizeStatus(log).getSeverity())
                        .thenComparing(LogEntry::getTimestamp))
                .map(log -> new TestOutcome(
                        normalizeStatus(log),
                        normalizeCategory(log),
                        resolveReason(log)))
                .orElseGet(TestOutcomeResolver::passOutcome);
    }

    private static OutcomeStatus normalizeStatus(LogEntry log) {
        if (log.getOutcomeStatus() != null) {
            return log.getOutcomeStatus();
        }

        Status status = log.getStatus();
        if (status == Status.FAIL) {
            return OutcomeStatus.FAIL;
        }
        if (status == Status.SKIP) {
            return OutcomeStatus.SKIP;
        }
        if (status == Status.PASS) {
            return OutcomeStatus.PASS;
        }
        return OutcomeStatus.INFO;
    }

    private static OutcomeCategory normalizeCategory(LogEntry log) {
        return log.getOutcomeCategory() != null ? log.getOutcomeCategory() : OutcomeCategory.LIFECYCLE;
    }

    private static String resolveReason(LogEntry log) {
        if (log.getOutcomeReason() != null && !log.getOutcomeReason().isBlank()) {
            return log.getOutcomeReason();
        }
        return log.getMessage();
    }

    private static TestOutcome passOutcome() {
        return new TestOutcome(OutcomeStatus.PASS, OutcomeCategory.LIFECYCLE, "Test passed.");
    }
}
