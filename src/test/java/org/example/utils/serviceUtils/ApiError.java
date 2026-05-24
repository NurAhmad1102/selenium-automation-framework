package org.example.utils.serviceUtils;

import org.openqa.selenium.devtools.v148.network.model.RequestId;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

public class ApiError {
    private final RequestId requestId;
    private final String name;
    private final String url;
    private final String normalizedUrl;
    private final String method;
    private final int statusCode;
    private final String statusText;
    private final String mimeType;
    private final String requestPayload;
    private final Instant timestamp;
    private volatile Instant lastSeenTimestamp;
    private final AtomicInteger retryCount = new AtomicInteger(1);
    private volatile String responseBody;
    private volatile String bodyCaptureError;

    public ApiError(RequestId requestId,
                    String name,
                    String url,
                    String normalizedUrl,
                    String method,
                    int statusCode,
                    String statusText,
                    String mimeType,
                    String requestPayload) {
        this.requestId = requestId;
        this.name = name;
        this.url = url;
        this.normalizedUrl = normalizedUrl;
        this.method = method;
        this.statusCode = statusCode;
        this.statusText = statusText;
        this.mimeType = mimeType;
        this.requestPayload = requestPayload;
        this.timestamp = Instant.now();
        this.lastSeenTimestamp = this.timestamp;
    }

    public RequestId getRequestId() {
        return requestId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getNormalizedUrl() {
        return normalizedUrl;
    }

    public String getMethod() {
        return method;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getRequestPayload() {
        return requestPayload;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Instant getLastSeenTimestamp() {
        return lastSeenTimestamp;
    }

    public int getRetryCount() {
        return retryCount.get();
    }

    public void incrementRetryCount() {
        retryCount.incrementAndGet();
        lastSeenTimestamp = Instant.now();
    }

    public String getResponseBody() {
        return responseBody;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public String getBodyCaptureError() {
        return bodyCaptureError;
    }

    public void setBodyCaptureError(String bodyCaptureError) {
        this.bodyCaptureError = bodyCaptureError;
    }

    public boolean hasResponseBody() {
        return responseBody != null && !responseBody.isEmpty();
    }
}
