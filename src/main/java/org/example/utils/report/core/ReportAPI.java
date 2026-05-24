package org.example.utils.report.core;

import com.aventstack.extentreports.Status;
import org.example.utils.report.internal.model.LogCategory;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.ReportData;
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
        log(LogCategory.DETAILS, Status.INFO, message);
    }

    public static void pass(String message) {
        log(LogCategory.DETAILS, Status.PASS, message);
    }

    public static void fail(String message) {
        log(LogCategory.DETAILS, Status.FAIL, message);
    }

    public static void fail(Throwable error) {
        LogEntry entry = new LogEntry(LogCategory.DETAILS, Status.FAIL, "Test Execution Failed");
        entry.setError(error);
        post(entry);
    }

    public static void fail(String message, Throwable error) {
        LogEntry entry = new LogEntry(LogCategory.DETAILS, Status.FAIL, message);
        entry.setError(error);
        post(entry);
    }

    public static void elementError(String locator, Throwable error) {
        elementError(null, locator, error);
    }

    public static void elementError(String action, String locator, Throwable error) {
        LogEntry entry = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        entry.setAction(action);
        entry.setLocator(locator);
        entry.setError(error);
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
        post(entry);
    }

    public static void addScreenshot(String path) {
        LogEntry entry = new LogEntry(LogCategory.ELEMENT_LOGS, Status.INFO, "Screenshot captured");
        entry.setScreenshotPath(path);
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
}
