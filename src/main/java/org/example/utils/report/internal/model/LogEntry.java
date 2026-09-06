package org.example.utils.report.internal.model;

import com.aventstack.extentreports.Status;
import java.time.Instant;

/**
 * Data model for a single log entry.
 */
public class LogEntry {
    private final Instant timestamp;
    private final LogCategory category;
    private final Status status;
    private final String message;
    
    // Detailed fields
    private String locator;
    private String action;
    private Throwable error;
    private String request;
    private String response;
    private String screenshotPath;
    private String name;
    private String statusText;
    private String mimeType;
    private int statusCode;
    private int occurrenceCount;
    private final long threadId;
    private int retryCount;
    private OutcomeStatus outcomeStatus;
    private OutcomeCategory outcomeCategory;
    private String outcomeReason;
    private boolean affectsFinalOutcome;
    private ElementSnapshot elementSnapshot;

    public LogEntry(LogCategory category, Status status, String message) {
        this(category, status, message, Instant.now());
    }

    public LogEntry(LogCategory category, Status status, String message, Instant timestamp) {
        this.timestamp = timestamp != null ? timestamp : Instant.now();
        this.category = category;
        this.status = status;
        this.message = message;
        this.threadId = Thread.currentThread().getId();
        this.outcomeStatus = mapStatus(status);
        this.outcomeCategory = LogCategory.NETWORK_LOGS == category
                ? OutcomeCategory.NETWORK
                : OutcomeCategory.LIFECYCLE;
        this.outcomeReason = message;
        this.affectsFinalOutcome = status == Status.PASS || status == Status.FAIL || status == Status.SKIP;
    }

    // Getters and Setters for optional fields
    public Instant getTimestamp() { return timestamp; }
    public LogCategory getCategory() { return category; }
    public Status getStatus() { return status; }
    public String getMessage() { return message; }
    public String getLocator() { return locator; }
    public void setLocator(String locator) { this.locator = locator; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Throwable getError() { return error; }
    public void setError(Throwable error) { this.error = error; }
    public String getRequest() { return request; }
    public void setRequest(String request) { this.request = request; }
    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }
    public String getScreenshotPath() { return screenshotPath; }
    public void setScreenshotPath(String screenshotPath) { this.screenshotPath = screenshotPath; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getMimeType() { return mimeType; }
    public void setMimeType(String mimeType) { this.mimeType = mimeType; }
    public int getStatusCode() { return statusCode; }
    public void setStatusCode(int statusCode) { this.statusCode = statusCode; }
    public int getOccurrenceCount() { return occurrenceCount; }
    public void setOccurrenceCount(int occurrenceCount) { this.occurrenceCount = occurrenceCount; }
    public long getThreadId() { return threadId; }
    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }
    public OutcomeStatus getOutcomeStatus() { return outcomeStatus; }
    public void setOutcomeStatus(OutcomeStatus outcomeStatus) { this.outcomeStatus = outcomeStatus; }
    public OutcomeCategory getOutcomeCategory() { return outcomeCategory; }
    public void setOutcomeCategory(OutcomeCategory outcomeCategory) { this.outcomeCategory = outcomeCategory; }
    public String getOutcomeReason() { return outcomeReason; }
    public void setOutcomeReason(String outcomeReason) { this.outcomeReason = outcomeReason; }
    public boolean isAffectsFinalOutcome() { return affectsFinalOutcome; }
    public void setAffectsFinalOutcome(boolean affectsFinalOutcome) { this.affectsFinalOutcome = affectsFinalOutcome; }
    public ElementSnapshot getElementSnapshot() { return elementSnapshot; }
    public void setElementSnapshot(ElementSnapshot elementSnapshot) { this.elementSnapshot = elementSnapshot; }

    private static OutcomeStatus mapStatus(Status status) {
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
}
