package com.automation.framework.core.report.core;

import com.automation.framework.core.report.internal.model.LogCategory;
import com.automation.framework.core.report.internal.model.ReportData;
import com.automation.framework.core.report.internal.storage.ThreadLocalStore;
import com.aventstack.extentreports.Status;

/**
 * Diagnostic helper responsible for parsing Selenium exceptions,
 * extracting locators, identifying root causes, and logging structured element errors.
 */
public final class SeleniumErrorDiagnosticHelper {

    public static class DiagnosticResult {
        private final String errorTitle;
        private final String potentialCause;
        private final String suggestion;
        private final String originLine;

        public DiagnosticResult(String errorTitle, String potentialCause, String suggestion, String originLine) {
            this.errorTitle = errorTitle != null ? errorTitle : "Element Error Occurred";
            this.potentialCause = potentialCause != null ? potentialCause : "Unknown cause";
            this.suggestion = suggestion != null ? suggestion : "Review stacktrace for details.";
            this.originLine = originLine;
        }

        public String getErrorTitle() {
            return errorTitle;
        }

        public String getPotentialCause() {
            return potentialCause;
        }

        public String getSuggestion() {
            return suggestion;
        }

        public String getOriginLine() {
            return originLine;
        }
    }

    private SeleniumErrorDiagnosticHelper() {
    }

    public static DiagnosticResult analyzeDiagnostic(Throwable throwable, String locator, String action) {
        String originLine = extractOriginLine(throwable);
        if (throwable == null) {
            return new DiagnosticResult("General Error", "No throwable details available", "Check test execution logs.", originLine);
        }

        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage() != null ? current.getMessage() : "";
            String lowerMessage = message.toLowerCase();

            // 1. Assertion Null / Empty Data
            if (throwable instanceof AssertionError && (lowerMessage.contains("null") || lowerMessage.contains("actual: []") || lowerMessage.contains("but found []"))) {
                return new DiagnosticResult(
                        "Null / Empty Test Data",
                        "Data expression or variable is null/empty. Assertion failed validating expected value.",
                        "Verify test data source and mappings are populated correctly.",
                        originLine
                );
            }

            // 2. Invalid Selector / XPath Syntax Error
            if (className.contains("InvalidSelectorException") || lowerMessage.contains("syntax error in xpath") || lowerMessage.contains("is not a valid xpath")) {
                return new DiagnosticResult(
                        "Invalid Selector / XPath Syntax",
                        "Locator syntax is invalid (unbalanced brackets or quotes).",
                        "Check selector or XPath syntax to ensure proper closure.",
                        originLine
                );
            }

            // 3. Stale Element Reference
            if (className.contains("StaleElementReferenceException") || lowerMessage.contains("stale element reference") || lowerMessage.contains("not attached to the page dom")) {
                return new DiagnosticResult(
                        "Stale Element Reference",
                        "The target element was removed or re-rendered in the DOM (SPA dynamic update).",
                        "Re-query the element immediately prior to interaction or use wait helper.",
                        originLine
                );
            }

            // 4. Element Click Intercepted
            if (className.contains("ElementClickInterceptedException") || lowerMessage.contains("click intercepted") || lowerMessage.contains("other element would receive the click")) {
                return new DiagnosticResult(
                        "Element Click Intercepted",
                        "Target element is obscured by an overlay (modal backdrop, toast, loading spinner).",
                        "Wait for overlay invisibility or use JavaScript click fallback if necessary.",
                        originLine
                );
            }

            // 5. Element Not Interactable / Hidden
            if (className.contains("ElementNotInteractableException") || className.contains("ElementNotVisibleException") || lowerMessage.contains("element not interactable") || lowerMessage.contains("is not visible")) {
                return new DiagnosticResult(
                        "Element Not Interactable / Hidden",
                        "Element is in DOM but hidden (display:none, visibility:hidden, or disabled).",
                        "Ensure parent container or dropdown is opened before interacting.",
                        originLine
                );
            }

            // 6. Move Target Out of Bounds
            if (className.contains("MoveTargetOutOfBoundsException") || lowerMessage.contains("out of bounds")) {
                return new DiagnosticResult(
                        "Element Out of Viewport",
                        "Element coordinates fall outside the visible viewport.",
                        "Scroll the element into view before interaction.",
                        originLine
                );
            }

            // 7. Timeout / NoSuchElement
            if (className.contains("NoSuchElementException") || className.contains("TimeoutException") || lowerMessage.contains("unable to locate element") || lowerMessage.contains("waiting for element")) {
                String target = (locator != null && !locator.isBlank()) ? locator : "target element";
                return new DiagnosticResult(
                        "Element Timeout / Not Found",
                        "Timed out waiting for element: " + target + ". Element was not located in DOM within timeout.",
                        "Check if page is still loading, selector updated, or element is inside an iframe.",
                        originLine
                );
            }

            // 8. Session Not Created
            if (className.contains("SessionNotCreatedException") || lowerMessage.contains("session not created") || lowerMessage.contains("chromedriver only supports")) {
                return new DiagnosticResult(
                        "Driver Version Mismatch",
                        "Browser driver is incompatible with the installed browser version.",
                        "Update browser driver version to match your installed browser.",
                        originLine
                );
            }

            // 9. Browser Crash / Disconnected
            if (className.contains("NoSuchSessionException") || lowerMessage.contains("err_connection_refused") || lowerMessage.contains("connection timed out")) {
                return new DiagnosticResult(
                        "Browser Disconnected / Crashed",
                        "Connection to browser was terminated or target server unreachable.",
                        "Check server network connection and test execution system resources.",
                        originLine
                );
            }

            Throwable cause = current.getCause();
            if (cause == null || cause == current) {
                break;
            }
            current = cause;
        }

        String fallbackTitle = throwable.getClass().getSimpleName();
        if (fallbackTitle.isBlank()) {
            fallbackTitle = "Execution Error";
        }
        String fallbackCause = throwable.getMessage() != null && !throwable.getMessage().isBlank()
                ? throwable.getMessage()
                : "Error occurred during step execution.";

        return new DiagnosticResult(fallbackTitle, fallbackCause, "Inspect stacktrace for failure details.", originLine);
    }

    public static String extractOriginLine(Throwable throwable) {
        if (throwable == null) {
            return null;
        }

        Throwable current = throwable;
        while (current != null) {
            for (StackTraceElement element : current.getStackTrace()) {
                String className = element.getClassName();
                if (isApplicationFrame(className)) {
                    String fileName = element.getFileName() != null ? element.getFileName() : className.substring(className.lastIndexOf('.') + 1) + ".java";
                    return fileName + ":" + element.getLineNumber() + " (" + element.getMethodName() + "())";
                }
            }
            Throwable cause = current.getCause();
            if (cause == null || cause == current) {
                break;
            }
            current = cause;
        }
        return null;
    }

    public static boolean isApplicationFrame(String className) {
        if (className == null) return false;
        return !className.startsWith("java.")
                && !className.startsWith("javax.")
                && !className.startsWith("jdk.")
                && !className.startsWith("sun.")
                && !className.startsWith("org.openqa.selenium.")
                && !className.startsWith("org.testng.")
                && !className.startsWith("com.automation.framework.core.")
                && !className.startsWith("com.aventstack.")
                && !className.startsWith("org.apache.logging.");
    }

    public static void logElementErrorIfApplicable(Throwable throwable) {
        if (throwable == null) {
            return;
        }

        try {
            ReportData reportData = ThreadLocalStore.get();
            if (reportData != null) {
                boolean alreadyHasElementError = reportData.getLogs().stream()
                        .anyMatch(l -> l.getCategory() == LogCategory.ELEMENT_LOGS
                                && (l.getError() != null || l.getStatus() == Status.FAIL));
                if (alreadyHasElementError) {
                    return; // Prevent duplicate element logging
                }
            }
        } catch (Exception ignored) {
        }

        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName();
            String message = current.getMessage();
            String lowerMessage = message != null ? message.toLowerCase() : "";

            if (message != null && message.startsWith("Failed to ") && message.contains("Locator Detail:")) {
                return;
            }

            if (lowerMessage.contains("the following asserts failed")) {
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
            } else if (className.contains("AssertionError") ||
                    lowerMessage.contains("the following asserts failed") ||
                    lowerMessage.contains("expected [") ||
                    lowerMessage.contains("assertion failed")) {
                isElementError = true;
                action = "Assertion Validation";
                locator = "Data Assertion";
            }

            if (isElementError) {
                ReportAPI.elementError(action, locator, throwable);
                return;
            }

            Throwable cause = current.getCause();
            if (cause == null || cause == current) {
                break;
            }
            current = cause;
        }
    }

    public static String extractLocatorFromMessage(String message) {
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

    public static String normalizeLocator(String locator) {
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
