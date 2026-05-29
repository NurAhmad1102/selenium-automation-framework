package org.example.utils.report.core;

import com.aventstack.extentreports.Status;
import org.example.utils.report.internal.model.LogCategory;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.OutcomeCategory;
import org.example.utils.report.internal.model.OutcomeStatus;
import org.example.utils.report.internal.model.ReportData;
import org.example.utils.report.internal.model.TestOutcome;
import org.example.utils.report.internal.storage.ThreadLocalStore;

import java.time.Instant;

/**
 * Main API for the framework to log events.
 */
public class ReportAPI {

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
        elementError(null, locator, error);
    }

    public static void elementError(String action, String locator, Throwable error) {
        LogEntry entry = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        entry.setAction(action);
        entry.setLocator(locator);
        entry.setError(error);
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

    private static String deriveNameFromUrl(String url) {
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

    private static String toTitleCase(String value) {
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
        if (throwable == null) {
            return;
        }

        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage();
            String lowerMessage = message != null ? message.toLowerCase() : "";

            if (message != null && message.startsWith("Failed to ") && message.contains("Locator Detail:")) {
                return;
            }

            boolean isElementError = false;
            String action = "Interaction";
            String locator = "Unknown Locator";

            if (className.contains("NoSuchElementException") ||
                    lowerMessage.contains("no such element") ||
                    lowerMessage.contains("unable to locate element")) {
                isElementError = true;
                action = "Locate Element";
                locator = extractLocatorFromMessage(message);
            } else if (className.contains("ElementNotInteractableException") ||
                    className.contains("ElementClickInterceptedException") ||
                    lowerMessage.contains("element not interactable") ||
                    lowerMessage.contains("click intercepted") ||
                    lowerMessage.contains("is not clickable") ||
                    lowerMessage.contains("clickability")) {
                isElementError = true;
                action = "Click/Interact Element";
                locator = extractLocatorFromMessage(message);
            } else if (className.contains("TimeoutException") ||
                    className.contains("StaleElementReferenceException") ||
                    lowerMessage.contains("waiting for element") ||
                    lowerMessage.contains("to be clickable") ||
                    lowerMessage.contains("to be visible") ||
                    lowerMessage.contains("stale element reference")) {
                isElementError = true;
                action = "Wait/Stale Element";
                locator = extractLocatorFromMessage(message);
            }

            if (isElementError) {
                LogEntry entry = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Selenium Element Interaction Failed");
                entry.setAction(action);
                entry.setLocator(locator);
                entry.setError(throwable);
                applyOutcome(entry, OutcomeStatus.FAIL, OutcomeCategory.ELEMENT, "Selenium Element Interaction Failed: " + message, true);
                post(entry);
                return;
            }

            Throwable cause = current.getCause();
            if (cause == null || cause == current) {
                break;
            }
            current = cause;
        }
    }

    private static String extractLocatorFromMessage(String message) {
        if (message == null || message.isBlank()) {
            return "Unknown Locator";
        }

        String elementLocator = extractAfterMarker(message, "-> ");
        if (elementLocator != null) {
            return elementLocator;
        }

        String defaultLocator = extractQuotedLocator(message, "DefaultElementLocator '");
        if (defaultLocator != null) {
            return normalizeLocator(defaultLocator);
        }

        String byLocator = extractByLocator(message);
        if (byLocator != null) {
            return normalizeLocator(byLocator);
        }

        int byIndex = message.indexOf("found by By.");
        if (byIndex >= 0) {
            int endLine = message.indexOf('\n', byIndex);
            if (endLine < 0) endLine = message.indexOf(" (tried for", byIndex);
            if (endLine < 0) endLine = message.length();
            return normalizeLocator(message.substring(byIndex, endLine).trim());
        }

        int selectorIndex = message.indexOf("\"selector\":\"");
        if (selectorIndex >= 0) {
            int start = selectorIndex + 12;
            int end = message.indexOf("\"", start);
            if (end >= start) {
                String method = "element";
                int methodIndex = message.indexOf("\"method\":\"");
                if (methodIndex >= 0) {
                    int mStart = methodIndex + 10;
                    int mEnd = message.indexOf("\"", mStart);
                    if (mEnd >= mStart) {
                        method = message.substring(mStart, mEnd);
                    }
                }
                return method + ": " + message.substring(start, end);
            }
        }

        int locateBy = message.indexOf("Unable to locate element: ");
        if (locateBy >= 0) {
            int start = locateBy + 26;
            int endLine = message.indexOf('\n', start);
            if (endLine < 0) endLine = message.length();
            return normalizeLocator(message.substring(start, endLine).trim());
        }

        int xpathIndex = findLikelyXpathStart(message);
        if (xpathIndex >= 0) {
            int end = message.indexOf("\"", xpathIndex);
            if (end < 0) end = message.indexOf("'", xpathIndex);
            if (end < 0) end = message.indexOf("\n", xpathIndex);
            if (end < 0) end = message.indexOf("]", xpathIndex);
            if (end < 0) end = message.length();
            return "xpath: " + message.substring(xpathIndex, end).trim();
        }

        return "Unknown Locator";
    }

    private static String extractAfterMarker(String message, String marker) {
        int markerIndex = message.indexOf(marker);
        while (markerIndex >= 0) {
            int start = markerIndex + marker.length();
            int end = message.indexOf("]]", start);
            if (end < 0) {
                end = message.indexOf('\n', start);
            }
            if (end < 0) {
                end = message.length();
            }

            String candidate = message.substring(start, end).trim();
            if (looksLikeLocator(candidate)) {
                return normalizeLocator(candidate);
            }
            markerIndex = message.indexOf(marker, start);
        }
        return null;
    }

    private static String extractQuotedLocator(String message, String marker) {
        int start = message.indexOf(marker);
        if (start < 0) {
            return null;
        }
        start += marker.length();
        int end = message.indexOf("'", start);
        if (end < 0) {
            return null;
        }
        String candidate = message.substring(start, end).trim();
        return looksLikeLocator(candidate) ? candidate : null;
    }

    private static String extractByLocator(String message) {
        String[] markers = {"By.xpath:", "By.cssSelector:", "By.id:", "By.name:", "By.className:", "By.tagName:", "By.linkText:", "By.partialLinkText:"};
        for (String marker : markers) {
            int start = message.indexOf(marker);
            if (start < 0) {
                continue;
            }
            int end = message.indexOf('\n', start);
            if (end < 0) {
                end = message.indexOf(" (Session info:", start);
            }
            if (end < 0) {
                end = message.indexOf("Build info:", start);
            }
            if (end < 0) {
                end = message.length();
            }
            return message.substring(start, end).trim();
        }
        return null;
    }

    private static int findLikelyXpathStart(String message) {
        int index = message.indexOf("//");
        while (index >= 0) {
            if (!isUrlSeparator(message, index)) {
                return index;
            }
            index = message.indexOf("//", index + 2);
        }
        return -1;
    }

    private static boolean isUrlSeparator(String message, int index) {
        return index > 0 && message.charAt(index - 1) == ':';
    }

    private static boolean looksLikeLocator(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }
        String lowerValue = value.toLowerCase();
        return lowerValue.startsWith("xpath:") ||
                lowerValue.startsWith("css selector:") ||
                lowerValue.startsWith("cssselector:") ||
                lowerValue.startsWith("id:") ||
                lowerValue.startsWith("name:") ||
                lowerValue.startsWith("classname:") ||
                lowerValue.startsWith("tagname:") ||
                lowerValue.startsWith("linktext:") ||
                lowerValue.startsWith("partiallinktext:") ||
                lowerValue.startsWith("by.") ||
                value.startsWith("//") ||
                value.startsWith("(//");
    }

    private static String normalizeLocator(String locator) {
        if (locator == null || locator.isBlank()) {
            return "Unknown Locator";
        }
        String normalized = locator.trim();
        if (normalized.startsWith("By.")) {
            normalized = normalized.substring(3);
        }
        normalized = normalized.replace("cssSelector:", "css selector:");
        return normalized;
    }
}
