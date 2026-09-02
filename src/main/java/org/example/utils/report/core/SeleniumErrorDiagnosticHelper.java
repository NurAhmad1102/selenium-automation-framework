package org.example.utils.report.core;

import com.aventstack.extentreports.Status;
import org.example.utils.report.internal.model.LogCategory;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.OutcomeCategory;
import org.example.utils.report.internal.model.OutcomeStatus;

/**
 * Diagnostic helper responsible for parsing Selenium exceptions,
 * extracting locators, and logging structured element errors.
 */
public final class SeleniumErrorDiagnosticHelper {

    private SeleniumErrorDiagnosticHelper() {
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
