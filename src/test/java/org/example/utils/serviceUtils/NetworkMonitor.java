package org.example.utils.serviceUtils;

import com.aventstack.extentreports.ExtentTest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.report.core.ReportAPI;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;
import org.openqa.selenium.devtools.HasDevTools;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class NetworkMonitor {

    private static final Logger logger = LogManager.getLogger(NetworkMonitor.class);
    private static final int MAX_RESPONSE_BODY_LENGTH = 1000;
    private static final ThreadLocal<NetworkMonitor> currentMonitor = new ThreadLocal<>();

    private final DevTools devTools;
    private final CdpVersionHelper cdpHelper;
    private final WebDriver webDriver;
    private final AtomicBoolean monitoringStarted = new AtomicBoolean(false);
    private final Map<String, RequestMetadata> requestMetadataById = new ConcurrentHashMap<>();
    private final Map<String, RequestMetadata> latestRequestMetadataByUrl = new ConcurrentHashMap<>();
    private final Map<String, PendingBodyCapture> pendingBodyCaptureById = new ConcurrentHashMap<>();
    private final Map<String, ApiError> apiErrorsByKey = new ConcurrentHashMap<>();

    public NetworkMonitor(HasDevTools driver) {
        this.devTools = driver.getDevTools();
        this.webDriver = (WebDriver) driver;
        this.cdpHelper = new CdpVersionHelper(this.webDriver);
    }

    public static NetworkMonitor getCurrentMonitor() {
        return currentMonitor.get();
    }

    public static void setCurrentMonitor(NetworkMonitor monitor) {
        currentMonitor.set(monitor);
    }

    public static void removeCurrentMonitor() {
        currentMonitor.remove();
    }

    @SuppressWarnings("unchecked")
    public void startMonitoring() {
        if (!monitoringStarted.compareAndSet(false, true)) {
            logger.debug("Network monitoring already started for current test.");
            return;
        }

        try {
            if (webDriver != null) {
                String windowHandle = webDriver.getWindowHandle();
                devTools.createSessionIfThereIsNotOne(windowHandle);
            } else {
                devTools.createSession();
            }
        } catch (Exception e) {
            try {
                devTools.createSession();
            } catch (Exception ex) {
                logger.warn("Could not create CDP session for network monitoring", ex);
            }
        }
        devTools.send(cdpHelper.enableNetwork());

        // Listen for requestWillBeSent event
        devTools.addListener((Event<Object>) cdpHelper.requestWillBeSentEvent(), requestWillBeSent -> {
            try {
                Object request = CdpVersionHelper.invokeGetter(requestWillBeSent, "getRequest");
                String method = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(request, "getMethod"));
                String url = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(request, "getUrl"));
                String resourceType = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(requestWillBeSent, "getType"));
                String postData = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(request, "getPostData"));
                Object requestId = CdpVersionHelper.invokeGetter(requestWillBeSent, "getRequestId");

                RequestMetadata metadata = new RequestMetadata(
                        method,
                        url,
                        normalizeUrl(url),
                        resourceType,
                        postData);

                requestMetadataById.put(requestIdKey(requestId), metadata);
                latestRequestMetadataByUrl.put(metadata.normalizedUrl, metadata);
            } catch (Exception e) {
                logger.error("Error processing requestWillBeSent event via reflection", e);
            }
        });

        // Listen for responseReceived event
        devTools.addListener((Event<Object>) cdpHelper.responseReceivedEvent(), responseReceived -> {
            try {
                Object response = CdpVersionHelper.invokeGetter(responseReceived, "getResponse");
                int statusCode = (Integer) CdpVersionHelper.invokeGetter(response, "getStatus");
                Object requestId = CdpVersionHelper.invokeGetter(responseReceived, "getRequestId");
                String requestIdKey = requestIdKey(requestId);
                String responseUrl = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(response, "getUrl"));
                String responseNormalizedUrl = normalizeUrl(responseUrl);
                
                RequestMetadata metadata = requestMetadataById.get(requestIdKey);
                if (metadata == null) {
                    metadata = latestRequestMetadataByUrl.get(responseNormalizedUrl);
                }
                if (metadata == null) {
                    logger.debug("Request metadata not found for requestId={} url={}", requestIdKey, responseNormalizedUrl);
                }
                
                String resourceType = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(responseReceived, "getType"));
                String method = resolveMethod(metadata);
                String url = metadata != null ? metadata.url : responseUrl;
                String normalizedUrl = metadata != null ? metadata.normalizedUrl : responseNormalizedUrl;
                String requestPayload = metadata != null ? metadata.postData : null;

                if (!shouldCapture(method, statusCode, resourceType)) {
                    return;
                }

                String statusText = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(response, "getStatusText"));
                String mimeType = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(response, "getMimeType"));

                String uniqueKey = buildUniqueKey(method, normalizedUrl, statusCode);
                ApiError apiError = apiErrorsByKey.compute(uniqueKey, (key, existingError) -> {
                    if (existingError != null) {
                        existingError.incrementRetryCount();
                        return existingError;
                    }

                    return new ApiError(
                            requestId,
                            deriveNameFromUrl(normalizedUrl),
                            url,
                            normalizedUrl,
                            method,
                            statusCode,
                            statusText,
                            mimeType,
                            limitBodyLength(requestPayload));
                });

                pendingBodyCaptureById.put(requestIdKey, new PendingBodyCapture(requestId, apiError));
                logger.warn("Detected HTTP {} for {} {}", statusCode, method, normalizedUrl);
            } catch (Exception e) {
                logger.error("Error processing responseReceived event via reflection", e);
            }
        });

        // Listen for loadingFailed event
        devTools.addListener((Event<Object>) cdpHelper.loadingFailedEvent(), loadingFailed -> {
            try {
                Object requestId = CdpVersionHelper.invokeGetter(loadingFailed, "getRequestId");
                String requestIdKey = requestIdKey(requestId);
                PendingBodyCapture pendingBodyCapture = pendingBodyCaptureById.remove(requestIdKey);
                if (pendingBodyCapture != null && pendingBodyCapture.apiError.getBodyCaptureError() == null) {
                    String errorText = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(loadingFailed, "getErrorText"));
                    pendingBodyCapture.apiError.setBodyCaptureError(errorText);
                }
                requestMetadataById.remove(requestIdKey);
            } catch (Exception e) {
                logger.error("Error processing loadingFailed event via reflection", e);
            }
        });

        // Listen for loadingFinished event
        devTools.addListener((Event<Object>) cdpHelper.loadingFinishedEvent(), loadingFinished -> {
            try {
                Object requestId = CdpVersionHelper.invokeGetter(loadingFinished, "getRequestId");
                String key = requestIdKey(requestId);
                requestMetadataById.remove(key);
                PendingBodyCapture pending = pendingBodyCaptureById.get(key);
                if (pending != null) {
                    captureResponseBody(pending.requestId, pending.apiError);
                    pendingBodyCaptureById.remove(key);
                }
            } catch (Exception e) {
                logger.error("Error processing loadingFinished event via reflection", e);
            }
        });
    }

    public void flushServerErrorsToReport(ExtentTest currentTest) {
        flushServerErrorsToReport(currentTest, Instant.now());
    }

    public void flushServerErrorsToReport(ExtentTest currentTest, Instant capturedBefore) {
        capturePendingResponseBodies();
        Instant upperBound = capturedBefore != null ? capturedBefore : Instant.now();

        apiErrorsByKey.values().stream()
                .filter(error -> !error.getLastSeenTimestamp().isAfter(upperBound))
                .sorted(Comparator.comparing(ApiError::getTimestamp))
                .forEach(error -> {
                    ReportAPI.network(
                            error.getName(),
                            error.getMethod(),
                            error.getNormalizedUrl(),
                            error.getStatusCode(),
                            error.getStatusText(),
                            error.getMimeType(),
                            error.getRetryCount(),
                            error.getRequestPayload(),
                            error.getResponseBody() != null ? error.getResponseBody() : error.getBodyCaptureError(),
                            error.getLastSeenTimestamp()
                    );
                });
    }

    public void stopMonitoring() {
        if (!monitoringStarted.get()) {
            return;
        }
        try {
            try {
                capturePendingResponseBodies();
            } catch (Exception e) {
                logger.debug("Failed to capture pending response bodies during stop: {}", e.getMessage());
            }

            try {
                devTools.send(cdpHelper.disableNetwork());
            } catch (Exception e) {
                logger.debug("Failed to disable network domain: {}", e.getMessage());
            }

            try {
                devTools.clearListeners();
            } catch (Exception e) {
                logger.debug("Failed to clear DevTools listeners: {}", e.getMessage());
            }

            try {
                devTools.close();
            } catch (Exception e) {
                logger.debug("Failed to close DevTools session: {}", e.getMessage());
            }
        } finally {
            requestMetadataById.clear();
            latestRequestMetadataByUrl.clear();
            pendingBodyCaptureById.clear();
            apiErrorsByKey.clear();
            monitoringStarted.set(false);
        }
    }

    private void capturePendingResponseBodies() {
        pendingBodyCaptureById.forEach((requestIdKey, pendingBodyCapture) -> {
            try {
                captureResponseBody(pendingBodyCapture.requestId, pendingBodyCapture.apiError);
            } finally {
                pendingBodyCaptureById.remove(requestIdKey);
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void captureResponseBody(Object requestId, ApiError apiError) {
        if (apiError.hasResponseBody()) {
            return;
        }

        try {
            Object getResponseBodyResponse = devTools.send(cdpHelper.getResponseBody(requestId));
            String body = CdpVersionHelper.getOptionalString(CdpVersionHelper.invokeGetter(getResponseBodyResponse, "getBody"));
            apiError.setResponseBody(limitBodyLength(body));
        } catch (Exception e) {
            apiError.setBodyCaptureError(e.getMessage());
            logger.warn("Failed to capture response body for {} {}: {}",
                    apiError.getMethod(),
                    apiError.getNormalizedUrl(),
                    e.getMessage());
        }
    }

    private static String requestIdKey(Object requestId) {
        return requestId == null ? "" : requestId.toString();
    }

    private static String resolveMethod(RequestMetadata metadata) {
        if (metadata == null || metadata.method == null || metadata.method.isBlank()) {
            return "GET";
        }
        return metadata.method;
    }

    private static boolean shouldCapture(String method, int statusCode, String resourceType) {
        if (statusCode < 400) {
            return false;
        }

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }

        return "XHR".equalsIgnoreCase(resourceType) || "Fetch".equalsIgnoreCase(resourceType);
    }

    private static String buildUniqueKey(String method, String normalizedUrl, int statusCode) {
        return method.toUpperCase() + " " + normalizedUrl + " " + statusCode;
    }

    private static String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        String baseUrl;
        try {
            URI uri = URI.create(url);
            baseUrl = new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    null,
                    null).toString();
        } catch (Exception e) {
            int queryIndex = url.indexOf('?');
            baseUrl = queryIndex >= 0 ? url.substring(0, queryIndex) : url;
        }

        try {
            baseUrl = baseUrl.replaceAll("(?<=/)[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}(?=/|$)", "{uuid}");
            baseUrl = baseUrl.replaceAll("(?<=/)\\d+(?=/|$)", "{id}");
        } catch (Exception e) {
            logger.debug("Failed to normalize URL path segments: {}", e.getMessage());
        }

        return baseUrl;
    }

    private static String limitBodyLength(String body) {
        if (body == null || body.isEmpty()) {
            return body;
        }

        if (body.length() <= MAX_RESPONSE_BODY_LENGTH) {
            return body;
        }

        return body.substring(0, MAX_RESPONSE_BODY_LENGTH)
                + "\n... response body truncated to " + MAX_RESPONSE_BODY_LENGTH + " characters";
    }

    private static String deriveNameFromUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "Unknown";
        }

        try {
            String path = URI.create(url).getPath();
            if (path == null || path.isEmpty()) {
                return "Unknown";
            }

            String[] segments = path.split("/");
            for (int index = segments.length - 1; index >= 0; index--) {
                String segment = segments[index];
                if (segment == null || segment.isBlank() || segment.matches("\\d+")) {
                    continue;
                }
                return toTitleCase(URLDecoder.decode(segment, StandardCharsets.UTF_8));
            }
        } catch (Exception e) {
            logger.warn("Failed to derive API name from URL {}: {}", url, e.getMessage());
        }

        return "Unknown";
    }

    private static String toTitleCase(String value) {
        String normalized = value.replaceAll("[_-]+", " ").trim();
        if (normalized.isEmpty()) {
            return "Unknown";
        }

        String[] words = normalized.split("\\s+");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
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
        return result.length() == 0 ? "Unknown" : result.toString();
    }

    private static class RequestMetadata {
        private final String method;
        private final String url;
        private final String normalizedUrl;
        private final String resourceType;
        private final String postData;

        private RequestMetadata(String method,
                                String url,
                                String normalizedUrl,
                                String resourceType,
                                String postData) {
            this.method = method;
            this.url = url;
            this.normalizedUrl = normalizedUrl;
            this.resourceType = resourceType;
            this.postData = postData;
        }
    }

    private static class PendingBodyCapture {
        private final Object requestId;
        private final ApiError apiError;

        private PendingBodyCapture(Object requestId, ApiError apiError) {
            this.requestId = requestId;
            this.apiError = apiError;
        }
    }
}
