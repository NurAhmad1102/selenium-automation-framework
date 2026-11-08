package com.automation.framework.core.report.core;

import com.automation.framework.core.report.internal.model.ElementSnapshot;
import com.automation.framework.core.report.internal.model.LogCategory;
import com.automation.framework.core.report.internal.model.LogEntry;
import com.automation.framework.core.report.internal.model.OutcomeCategory;
import com.automation.framework.core.report.internal.model.OutcomeStatus;
import com.automation.framework.core.report.internal.model.ReportData;
import com.automation.framework.core.report.internal.model.TestOutcome;
import com.automation.framework.core.report.internal.storage.ThreadLocalStore;
import com.aventstack.extentreports.Status;

import java.time.Instant;
import java.util.List;

/**
 * Public static API for logging test execution events, element interactions, and network diagnostics.
 */
public final class ReportAPI {

    private ReportAPI() {
    }

    public static void startTest(String testName, String description) {
        ReportData data = ThreadLocalStore.get();
        data.clear();
        data.setActive(true);
        ReportEventBus.startTest(testName, description);
    }

    public static void step(String message) {
        log(LogCategory.DETAILS, Status.INFO, message, OutcomeStatus.INFO, OutcomeCategory.LIFECYCLE, false);
    }

    public static void pass(String message) {
        log(LogCategory.DETAILS, Status.PASS, message, OutcomeStatus.PASS, OutcomeCategory.LIFECYCLE, true);
    }

    public static void fail(String message) {
        fail(message, OutcomeCategory.MANUAL);
    }

    public static void fail(String message, OutcomeCategory category) {
        log(LogCategory.DETAILS, Status.FAIL, message, OutcomeStatus.FAIL, category, true);
    }

    public static void fail(Throwable error) {
        fail("Test Execution Failed", error);
    }

    public static void fail(String message, Throwable error) {
        logElementErrorIfApplicable(error);
        LogEntry entry = new LogEntry(LogCategory.DETAILS, Status.FAIL, message);
        entry.setError(error);
        TestOutcome outcome = ThrowableOutcomeClassifier.classify(error);
        OutcomeStatus status = outcome.getStatus() == OutcomeStatus.BROKEN || outcome.getStatus() == OutcomeStatus.BLOCKED
                ? outcome.getStatus()
                : OutcomeStatus.FAIL;
        applyOutcome(entry, status, outcome.getCategory(), message, true);
        post(entry);
    }

    public static void broken(String message, Throwable error) {
        logElementErrorIfApplicable(error);
        LogEntry entry = new LogEntry(LogCategory.DETAILS, Status.FAIL, message);
        entry.setError(error);
        applyOutcome(entry, OutcomeStatus.BROKEN, OutcomeCategory.INFRASTRUCTURE, message, true);
        post(entry);
    }

    public static void blocked(String message, Throwable error) {
        logElementErrorIfApplicable(error);
        LogEntry entry = new LogEntry(LogCategory.DETAILS, Status.SKIP, message);
        entry.setError(error);
        applyOutcome(entry, OutcomeStatus.BLOCKED, OutcomeCategory.DEPENDENCY, message, true);
        post(entry);
    }

    public static void skip(String message) {
        log(LogCategory.DETAILS, Status.SKIP, message, OutcomeStatus.SKIP, OutcomeCategory.LIFECYCLE, true);
    }

    public static void skip(Throwable error) {
        logElementErrorIfApplicable(error);
        LogEntry entry = new LogEntry(LogCategory.DETAILS, Status.SKIP, "Test Execution Skipped");
        entry.setError(error);
        applyOutcome(entry, OutcomeStatus.SKIP, OutcomeCategory.LIFECYCLE, "Test Execution Skipped", true);
        post(entry);
    }

    public static void skip(String message, Throwable error) {
        logElementErrorIfApplicable(error);
        LogEntry entry = new LogEntry(LogCategory.DETAILS, Status.SKIP, message);
        entry.setError(error);
        applyOutcome(entry, OutcomeStatus.SKIP, OutcomeCategory.LIFECYCLE, message, true);
        post(entry);
    }

    public static void warning(String message) {
        log(LogCategory.DETAILS, Status.INFO, message, OutcomeStatus.WARNING, OutcomeCategory.DIAGNOSTIC, false);
    }

    public static void elementError(String locator, Throwable error) {
        elementError(null, locator, error, null);
    }

    public static void elementError(String action, String locator, Throwable error) {
        elementError(action, locator, error, null);
    }

    public static void elementError(String action, String locator, Throwable error, ElementSnapshot snapshot) {
        elementError(action, locator, error, snapshot, null);
    }

    public static void elementError(String action, String locator, Throwable error, ElementSnapshot snapshot, String screenshotPath) {
        LogEntry entry = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        entry.setAction(action);
        entry.setLocator(locator);
        entry.setError(error);
        entry.setElementSnapshot(snapshot);
        entry.setScreenshotPath(screenshotPath);
        applyOutcome(entry, OutcomeStatus.FAIL, OutcomeCategory.ELEMENT, "Element Error Occurred", true);
        post(entry);
    }

    public static void elementWarning(String action, String locator, Throwable error) {
        LogEntry entry = new LogEntry(LogCategory.ELEMENT_LOGS, Status.INFO, "Element Diagnostic Warning");
        entry.setAction(action);
        entry.setLocator(locator);
        entry.setError(error);
        applyOutcome(entry, OutcomeStatus.WARNING, OutcomeCategory.ELEMENT, "Element Diagnostic Warning", false);
        post(entry);
    }

    public static void network(String method, String url, int statusCode, String request, String response) {
        network(method, url, statusCode, request, response, Instant.now());
    }

    public static void network(String method,
                               String url,
                               int statusCode,
                               String request,
                               String response,
                               Instant eventTimestamp) {
        network(deriveNameFromUrl(url), method, url, statusCode, null, null, 1, request, response, eventTimestamp);
    }

    public static void network(String name,
                               String method,
                               String url,
                               int statusCode,
                               String statusText,
                               String mimeType,
                               int occurrenceCount,
                               String request,
                               String response,
                               Instant eventTimestamp) {
        LogEntry entry = new LogEntry(LogCategory.NETWORK_LOGS, Status.INFO,
                (name == null || name.isBlank() ? deriveNameFromUrl(url) : name) + " API - HTTP " + statusCode,
                eventTimestamp);
        entry.setName(name);
        entry.setAction(method);
        entry.setLocator(url);
        entry.setStatusCode(statusCode);
        entry.setStatusText(statusText);
        entry.setMimeType(mimeType);
        entry.setOccurrenceCount(occurrenceCount);
        entry.setRetryCount(statusCode);
        entry.setRequest(request);
        entry.setResponse(response);
        applyOutcome(entry, OutcomeStatus.INFO, OutcomeCategory.NETWORK, entry.getMessage(), false);
        post(entry);
    }

    public static void addScreenshot(String path) {
        LogEntry entry = new LogEntry(LogCategory.ELEMENT_LOGS, Status.INFO, "Screenshot captured");
        entry.setScreenshotPath(path);
        applyOutcome(entry, OutcomeStatus.INFO, OutcomeCategory.DIAGNOSTIC, "Screenshot captured", false);
        post(entry);
    }

    public static void attachScreenshotToLastFailureOrAdd(String path) {
        if (path == null || path.isBlank()) return;
        ReportData data = ThreadLocalStore.get();
        if (data != null) {
            List<LogEntry> logs = data.getLogs();
            for (int i = logs.size() - 1; i >= 0; i--) {
                LogEntry entry = logs.get(i);
                if (entry.getCategory() == LogCategory.ELEMENT_LOGS && (entry.getError() != null || entry.getStatus() == Status.FAIL)) {
                    if (entry.getScreenshotPath() == null || entry.getScreenshotPath().isBlank()) {
                        entry.setScreenshotPath(path);
                    }
                    return;
                }
            }
        }
        addScreenshot(path);
    }

    public static void flush() {
        ReportData data = ThreadLocalStore.get();
        ReportEventBus.flush(data);
        data.setActive(false);
        ThreadLocalStore.clear();
    }

    public static boolean isTestActive() {
        return ThreadLocalStore.get().isActive();
    }

    private static void log(LogCategory category, Status status, String message) {
        LogEntry entry = new LogEntry(category, status, message);
        post(entry);
    }

    private static void log(LogCategory category,
                            Status status,
                            String message,
                            OutcomeStatus outcomeStatus,
                            OutcomeCategory outcomeCategory,
                            boolean affectsFinalOutcome) {
        LogEntry entry = new LogEntry(category, status, message);
        applyOutcome(entry, outcomeStatus, outcomeCategory, message, affectsFinalOutcome);
        post(entry);
    }

    private static void applyOutcome(LogEntry entry,
                                     OutcomeStatus outcomeStatus,
                                     OutcomeCategory outcomeCategory,
                                     String reason,
                                     boolean affectsFinalOutcome) {
        entry.setOutcomeStatus(outcomeStatus);
        entry.setOutcomeCategory(outcomeCategory);
        entry.setOutcomeReason(reason);
        entry.setAffectsFinalOutcome(affectsFinalOutcome);
    }

    private static void post(LogEntry entry) {
        ThreadLocalStore.get().addLog(entry);
        ReportEventBus.postLog(entry);
    }

    public static String deriveNameFromUrl(String url) {
        if (url == null || url.isBlank()) {
            return "Unknown";
        }

        String path = url;
        int queryIndex = path.indexOf('?');
        if (queryIndex >= 0) {
            path = path.substring(0, queryIndex);
        }

        String[] segments = path.split("/");
        for (int index = segments.length - 1; index >= 0; index--) {
            String segment = segments[index];
            if (segment == null || segment.isBlank() || segment.matches("\\d+|\\{id}|\\{uuid}")) {
                continue;
            }
            return toTitleCase(segment);
        }
        return "Unknown";
    }

    public static String toTitleCase(String value) {
        String normalized = value.replaceAll("[_-]+", " ").trim();
        if (normalized.isEmpty()) {
            return "Unknown";
        }

        StringBuilder result = new StringBuilder();
        for (String word : normalized.split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(Character.toUpperCase(word.charAt(0)));
            if (word.length() > 1) {
                result.append(word.substring(1).toLowerCase());
            }
        }
        return result.toString();
    }

    public static boolean isInfrastructureSkip(Throwable throwable) {
        return ThrowableOutcomeClassifier.isInfrastructureSkip(throwable);
    }

    public static void logElementErrorIfApplicable(Throwable throwable) {
        SeleniumErrorDiagnosticHelper.logElementErrorIfApplicable(throwable);
    }
}
