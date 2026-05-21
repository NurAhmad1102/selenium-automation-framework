package org.example.utils.serviceUtils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.markuputils.CodeLanguage;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.HasDevTools;
import org.openqa.selenium.devtools.v139.network.Network;
import org.openqa.selenium.devtools.v139.network.model.RequestId;
import org.openqa.selenium.devtools.v139.network.model.Response;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NetworkMonitor {

    private static final Logger logger = LogManager.getLogger(NetworkMonitor.class);

    private final DevTools devTools;
    private final Map<RequestId, NetworkError> pendingServerErrors = new ConcurrentHashMap<>();
    private final Queue<NetworkError> serverErrors = new ConcurrentLinkedQueue<>();

    public NetworkMonitor(HasDevTools driver) {
        this.devTools = driver.getDevTools();
    }

    public void startMonitoring() {
        devTools.createSession();
        devTools.send(Network.enable(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));

        devTools.addListener(Network.responseReceived(), responseReceived -> {
            Response response = responseReceived.getResponse();
            int statusCode = response.getStatus();
            String url = response.getUrl();

            if (statusCode >= 500) {
                NetworkError error = new NetworkError(
                        responseReceived.getRequestId(),
                        url,
                        statusCode,
                        response.getStatusText(),
                        response.getMimeType());
                pendingServerErrors.put(responseReceived.getRequestId(), error);
                logger.warn("Detected HTTP {} for URL: {}", statusCode, url);
            }
        });

        devTools.addListener(Network.loadingFinished(), loadingFinished -> {
            NetworkError error = pendingServerErrors.remove(loadingFinished.getRequestId());
            if (error == null) {
                return;
            }

            try {
                String body = devTools.send(Network.getResponseBody(error.requestId)).getBody();
                error.setResponseBody(body);
            } catch (Exception e) {
                error.setBodyCaptureError(e.getMessage());
                logger.warn("Failed to capture response body for {}: {}", error.url, e.getMessage());
            }
            serverErrors.add(error);
        });

        devTools.addListener(Network.loadingFailed(), loadingFailed -> {
            NetworkError error = pendingServerErrors.remove(loadingFailed.getRequestId());
            if (error == null) {
                return;
            }

            error.setBodyCaptureError(loadingFailed.getErrorText());
            serverErrors.add(error);
        });
    }

    public void flushServerErrorsToReport(ExtentTest currentTest) {
        if (currentTest == null) {
            logger.warn("ExtentTest is null. Network errors cannot be written to ExtentReports.");
            return;
        }

        pendingServerErrors.values().forEach(serverErrors::add);
        pendingServerErrors.clear();

        NetworkError error;
        while ((error = serverErrors.poll()) != null) {
            currentTest.info("<b>API Error Captured During Failed Test (HTTP " + error.statusCode + ")</b>");
            currentTest.info("<b>URL:</b> " + error.url);
            currentTest.info("<b>Status:</b> " + error.statusCode + " " + error.statusText);

            if (error.mimeType != null && !error.mimeType.isEmpty()) {
                currentTest.info("<b>Mime Type:</b> " + error.mimeType);
            }

            if (error.responseBody != null && !error.responseBody.isEmpty()) {
                currentTest.info("<b>Response Body:</b>");
                currentTest.info(MarkupHelper.createCodeBlock(error.responseBody, CodeLanguage.JSON));
            } else if (error.bodyCaptureError != null && !error.bodyCaptureError.isEmpty()) {
                currentTest.info("<i>Could not capture response body: " + error.bodyCaptureError + "</i>");
            }
        }
    }

    public void stopMonitoring() {
        try {
            devTools.send(Network.disable());
            devTools.close();
        } catch (Exception e) {
            // Silently close
        }
    }

    private static class NetworkError {
        private final RequestId requestId;
        private final String url;
        private final int statusCode;
        private final String statusText;
        private final String mimeType;
        private String responseBody;
        private String bodyCaptureError;

        private NetworkError(RequestId requestId, String url, int statusCode, String statusText, String mimeType) {
            this.requestId = requestId;
            this.url = url;
            this.statusCode = statusCode;
            this.statusText = statusText;
            this.mimeType = mimeType;
        }

        private void setResponseBody(String responseBody) {
            this.responseBody = responseBody;
        }

        private void setBodyCaptureError(String bodyCaptureError) {
            this.bodyCaptureError = bodyCaptureError;
        }
    }
}
