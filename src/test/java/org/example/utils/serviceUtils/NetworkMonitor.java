package org.example.utils.serviceUtils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v148.network.Network;
import org.openqa.selenium.devtools.v148.network.model.Request;
import org.openqa.selenium.devtools.v148.network.model.RequestId;
import org.openqa.selenium.devtools.v148.network.model.ResourceType;
import org.openqa.selenium.devtools.v148.network.model.Response;

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
    private final AtomicBoolean monitoringStarted = new AtomicBoolean(false);
    private final Map<String, RequestMetadata> requestMetadataById = new ConcurrentHashMap<>();
    private final Map<String, RequestMetadata> latestRequestMetadataByUrl = new ConcurrentHashMap<>();
    private final Map<String, PendingBodyCapture> pendingBodyCaptureById = new ConcurrentHashMap<>();
    private final Map<String, ApiError> apiErrorsByKey = new ConcurrentHashMap<>();

    public NetworkMonitor(HasDevTools driver) {
        this.devTools = driver.getDevTools();
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

    public void startMonitoring() {
        if (!monitoringStarted.compareAndSet(false, true)) {
            logger.debug("Network monitoring already started for current test.");
            return;
        }

        devTools.createSession();
        devTools.send(Network.enable(
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty()));

        devTools.addListener(Network.requestWillBeSent(), requestWillBeSent -> {
            Request request = requestWillBeSent.getRequest();
            ResourceType resourceType = requestWillBeSent.getType().orElse(null);
            RequestMetadata metadata = new RequestMetadata(
                    request.getMethod(),
                    request.getUrl(),
                    normalizeUrl(request.getUrl()),
                    resourceType,
                    request.getPostData().orElse(null));

            requestMetadataById.put(requestIdKey(requestWillBeSent.getRequestId()), metadata);
            latestRequestMetadataByUrl.put(metadata.normalizedUrl, metadata);
        });

        devTools.addListener(Network.responseReceived(), responseReceived -> {
            Response response = responseReceived.getResponse();
            int statusCode = response.getStatus();
            String requestIdKey = requestIdKey(responseReceived.getRequestId());
            String responseUrl = response.getUrl();
            String responseNormalizedUrl = normalizeUrl(responseUrl);
            RequestMetadata metadata = requestMetadataById.get(requestIdKey);
            if (metadata == null) {
                metadata = latestRequestMetadataByUrl.get(responseNormalizedUrl);
            }
            if (metadata == null) {
                logger.debug("Request metadata not found for requestId={} url={}", requestIdKey, responseNormalizedUrl);
            }
            ResourceType resourceType = responseReceived.getType();
            String method = resolveMethod(metadata);
            String url = metadata != null ? metadata.url : responseUrl;
            String normalizedUrl = metadata != null ? metadata.normalizedUrl : responseNormalizedUrl;
            String requestPayload = metadata != null ? metadata.postData : null;

            if (!shouldCapture(method, statusCode, resourceType)) {
                return;
            }

            String uniqueKey = buildUniqueKey(method, normalizedUrl, statusCode);
            ApiError apiError = apiErrorsByKey.compute(uniqueKey, (key, existingError) -> {
                if (existingError != null) {
                    existingError.incrementRetryCount();
                    return existingError;
                }

                return new ApiError(
                        responseReceived.getRequestId(),
                        deriveNameFromUrl(normalizedUrl),
                        url,
                        normalizedUrl,
                        method,
                        statusCode,
                        response.getStatusText(),
                        response.getMimeType(),
                        limitBodyLength(requestPayload));
            });

            pendingBodyCaptureById.put(requestIdKey, new PendingBodyCapture(responseReceived.getRequestId(), apiError));
            logger.warn("Detected HTTP {} for {} {}", statusCode, method, normalizedUrl);
        });

        devTools.addListener(Network.loadingFailed(), loadingFailed -> {
            String requestIdKey = requestIdKey(loadingFailed.getRequestId());
            PendingBodyCapture pendingBodyCapture = pendingBodyCaptureById.remove(requestIdKey);
            if (pendingBodyCapture != null && pendingBodyCapture.apiError.getBodyCaptureError() == null) {
                pendingBodyCapture.apiError.setBodyCaptureError(loadingFailed.getErrorText());
            }
            requestMetadataById.remove(requestIdKey);
        });

        devTools.addListener(Network.loadingFinished(), loadingFinished -> {
            requestMetadataById.remove(requestIdKey(loadingFinished.getRequestId()));
        });
    }

    public void flushServerErrorsToReport(ExtentTest currentTest) {
        flushServerErrorsToReport(currentTest, Instant.now());
    }

    public void flushServerErrorsToReport(ExtentTest currentTest, Instant capturedBefore) {
        if (currentTest == null) {
            logger.warn("ExtentTest is null. Network errors cannot be written to ExtentReports.");
            return;
        }

        capturePendingResponseBodies();

        Optional<ApiError> latestError = apiErrorsByKey.values().stream()
                .filter(error -> capturedBefore == null || !error.getLastSeenTimestamp().isAfter(capturedBefore))
                .max(Comparator.comparing(ApiError::getLastSeenTimestamp));

        latestError.ifPresent(error -> {
            currentTest.info("<b>" + error.getName() + " API - HTTP "
                    + error.getStatusCode() + " - occurred " + error.getRetryCount() + " times</b>");
            currentTest.info("<b>Name:</b> " + error.getName());
            currentTest.info("<b>Method:</b> " + error.getMethod());
            currentTest.info("<b>URL:</b> " + error.getNormalizedUrl());
            currentTest.info("<b>Status:</b> " + error.getStatusCode() + " " + error.getStatusText());
            currentTest.info("<b>Timestamp:</b> " + error.getTimestamp());

            if (error.getMimeType() != null && !error.getMimeType().isEmpty()) {
                currentTest.info("<b>Mime Type:</b> " + error.getMimeType());
            }

            if (error.getRequestPayload() != null && !error.getRequestPayload().isEmpty()) {
                currentTest.info("<b>Request Payload:</b>");
                currentTest.info(MarkupHelper.createCodeBlock(error.getRequestPayload(), CodeLanguage.JSON));
            }

            if (error.hasResponseBody()) {
                currentTest.info("<b>Response:</b>");
                currentTest.info(MarkupHelper.createCodeBlock(error.getResponseBody(), CodeLanguage.JSON));
            } else if (error.getBodyCaptureError() != null && !error.getBodyCaptureError().isEmpty()) {
                currentTest.info("<i>Could not capture response body: " + error.getBodyCaptureError() + "</i>");
            } else {
                currentTest.info("<i>Response body is empty.</i>");
            }
        });
    }

    public void stopMonitoring() {
        try {
            if (monitoringStarted.get()) {
                capturePendingResponseBodies();
                devTools.send(Network.disable());
                devTools.close();
            }
        } catch (Exception e) {
            logger.debug("Failed to stop network monitoring cleanly: {}", e.getMessage());
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
            ApiError apiError = pendingBodyCapture.apiError;
            if (apiError.hasResponseBody()) {
                pendingBodyCaptureById.remove(requestIdKey);
                return;
            }

            try {
                String body = devTools.send(Network.getResponseBody(pendingBodyCapture.requestId)).getBody();
                apiError.setResponseBody(limitBodyLength(body));
            } catch (Exception e) {
                apiError.setBodyCaptureError(e.getMessage());
                logger.warn("Failed to capture response body for {} {}: {}",
                        apiError.getMethod(),
                        apiError.getNormalizedUrl(),
                        e.getMessage());
            } finally {
                pendingBodyCaptureById.remove(requestIdKey);
            }
        });
    }

    private static String requestIdKey(RequestId requestId) {
        return requestId == null ? "" : requestId.toString();
    }

    private static String resolveMethod(RequestMetadata metadata) {
        if (metadata == null || metadata.method == null || metadata.method.isBlank()) {
            return "GET";
        }
        return metadata.method;
    }

    private static boolean shouldCapture(String method, int statusCode, ResourceType resourceType) {
        if (statusCode < 500) {
            return false;
        }

        if ("OPTIONS".equalsIgnoreCase(method)) {
            return false;
        }

        return ResourceType.XHR.equals(resourceType) || ResourceType.FETCH.equals(resourceType);
    }

    private static String buildUniqueKey(String method, String normalizedUrl, int statusCode) {
        return method.toUpperCase() + " " + normalizedUrl + " " + statusCode;
    }

    private static String normalizeUrl(String url) {
        if (url == null || url.isEmpty()) {
            return "";
        }

        try {
            URI uri = URI.create(url);
            return new URI(
                    uri.getScheme(),
                    uri.getAuthority(),
                    uri.getPath(),
                    null,
                    null).toString();
        } catch (Exception e) {
            int queryIndex = url.indexOf('?');
            return queryIndex >= 0 ? url.substring(0, queryIndex) : url;
        }
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
        private final ResourceType resourceType;
        private final String postData;

        private RequestMetadata(String method,
                                String url,
                                String normalizedUrl,
                                ResourceType resourceType,
                                String postData) {
            this.method = method;
            this.url = url;
            this.normalizedUrl = normalizedUrl;
            this.resourceType = resourceType;
            this.postData = postData;
        }
    }

    private static class PendingBodyCapture {
        private final RequestId requestId;
        private final ApiError apiError;

        private PendingBodyCapture(RequestId requestId, ApiError apiError) {
            this.requestId = requestId;
            this.apiError = apiError;
        }
    }
}
