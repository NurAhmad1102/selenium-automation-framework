package org.example.utils.report.core;

import org.example.utils.report.internal.model.ElementSnapshot;
import org.openqa.selenium.*;

import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Inspector utility to capture the 6 diagnostic pillars of an element
 * from the live WebDriver instance upon failure or interaction errors.
 */
public class ElementSnapshotInspector {

    private static final Pattern LOCATOR_STRATEGY_PATTERN = Pattern.compile("^By\\.(\\w+):\\s*(.*)$");
    private static final Pattern INTERCEPT_PATTERN = Pattern.compile("(?i)Other element (.*?)(?: would receive the click| is not clickable)");

    public static ElementSnapshot captureSnapshot(WebDriver driver, Object target, String action, Throwable error, Duration timeout, Duration elapsed) {
        ElementSnapshot snapshot = new ElementSnapshot();
        
        // 1. Context & Wait Metadata
        snapshot.setActionType(action != null ? action : "Element Action");
        if (timeout != null) {
            snapshot.setWaitTimeout(timeout.getSeconds() + "s");
        }
        if (elapsed != null) {
            snapshot.setElapsedTime(String.format("%.1fs", elapsed.toMillis() / 1000.0));
        }

        // 2. Error Signature
        if (error != null) {
            snapshot.setExceptionName(error.getClass().getSimpleName());
            snapshot.setConciseErrorMessage(extractConciseErrorMessage(error.getMessage()));
        }

        // 3. Locator Identity Parsing
        parseLocatorIdentity(target, snapshot);

        // 4. Intercept / Obstruction Details Parsing
        if (error != null && error.getMessage() != null) {
            Matcher interceptMatcher = INTERCEPT_PATTERN.matcher(error.getMessage());
            if (interceptMatcher.find()) {
                snapshot.setOverlayElement(interceptMatcher.group(1).trim());
            }
        }

        // 5. Live DOM & State Inspection (Fail-Safe)
        if (driver != null) {
            try {
                inspectLiveElementState(driver, target, snapshot);
            } catch (Exception ignored) {
                // Safeguard against secondary exceptions
            }
        }

        return snapshot;
    }

    private static void parseLocatorIdentity(Object target, ElementSnapshot snapshot) {
        if (target == null) {
            snapshot.setLocatorStrategy("Unknown");
            snapshot.setLocatorValue("N/A");
            snapshot.setResolvedLocator("N/A");
            return;
        }

        String targetStr = target.toString().trim();
        snapshot.setResolvedLocator(targetStr);

        Matcher matcher = LOCATOR_STRATEGY_PATTERN.matcher(targetStr);
        if (matcher.find()) {
            snapshot.setLocatorStrategy(matcher.group(1));
            snapshot.setLocatorValue(matcher.group(2));
        } else if (targetStr.startsWith("xpath:")) {
            snapshot.setLocatorStrategy("xpath");
            snapshot.setLocatorValue(targetStr.substring(6).trim());
        } else if (targetStr.startsWith("css:")) {
            snapshot.setLocatorStrategy("cssSelector");
            snapshot.setLocatorValue(targetStr.substring(4).trim());
        } else if (target instanceof By) {
            snapshot.setLocatorStrategy("By");
            snapshot.setLocatorValue(targetStr);
        } else {
            snapshot.setLocatorStrategy("WebElement");
            snapshot.setLocatorValue(targetStr);
        }
    }

    @SuppressWarnings("unchecked")
    private static void inspectLiveElementState(WebDriver driver, Object target, ElementSnapshot snapshot) {
        WebElement element = null;
        if (target instanceof WebElement) {
            element = (WebElement) target;
        } else if (target instanceof By) {
            try {
                element = driver.findElement((By) target);
            } catch (Exception ignored) {}
        }

        if (element != null) {
            try {
                snapshot.setDisplayed(element.isDisplayed());
                snapshot.setEnabled(element.isEnabled());
                snapshot.setSelected(element.isSelected());
            } catch (Exception ignored) {}

            try {
                Rectangle rect = element.getRect();
                snapshot.setBoundingBox(String.format("x: %d, y: %d, w: %d, h: %d", rect.x, rect.y, rect.width, rect.height));
            } catch (Exception ignored) {}

            if (driver instanceof JavascriptExecutor) {
                try {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    String script = """
                        var el = arguments[0];
                        if (!el) return null;
                        var s = window.getComputedStyle(el);
                        return {
                            visibility: s.visibility,
                            display: s.display,
                            opacity: s.opacity,
                            pointerEvents: s.pointerEvents,
                            outerHtml: el.outerHTML ? el.outerHTML.substring(0, 500) : ''
                        };
                    """;
                    Map<String, Object> res = (Map<String, Object>) js.executeScript(script, element);
                    if (res != null) {
                        if (res.containsKey("visibility")) snapshot.addComputedStyle("visibility", String.valueOf(res.get("visibility")));
                        if (res.containsKey("display")) snapshot.addComputedStyle("display", String.valueOf(res.get("display")));
                        if (res.containsKey("opacity")) snapshot.addComputedStyle("opacity", String.valueOf(res.get("opacity")));
                        if (res.containsKey("pointerEvents")) snapshot.addComputedStyle("pointer-events", String.valueOf(res.get("pointerEvents")));
                        if (res.containsKey("outerHtml") && res.get("outerHtml") != null) {
                            snapshot.setOuterHtml(String.valueOf(res.get("outerHtml")));
                        }
                    }
                } catch (Exception ignored) {}
            }
        } else {
            // Target is not currently located in DOM (e.g., NoSuchElementException)
            snapshot.setDisplayed(false);
            snapshot.setEnabled(false);
            
            if (driver instanceof JavascriptExecutor) {
                try {
                    JavascriptExecutor js = (JavascriptExecutor) driver;
                    // Check if modal or overlay or iframe is active
                    String containerScript = """
                        var activeModal = document.querySelector('.modal.show, [role="dialog"], .ant-modal, .swal2-container');
                        if (activeModal) {
                            return 'Active Modal: ' + activeModal.outerHTML.substring(0, 300);
                        }
                        var iframes = document.querySelectorAll('iframe');
                        if (iframes.length > 0) {
                            return 'Warning: Found ' + iframes.length + ' iframe(s) in DOM. Target might be inside an unswitched iframe.';
                        }
                        return null;
                    """;
                    Object containerRes = js.executeScript(containerScript);
                    if (containerRes != null) {
                        snapshot.setParentContainerHtml(String.valueOf(containerRes));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private static String extractConciseErrorMessage(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) return "No specific error message.";
        String cleaned = rawMessage;
        int buildIdx = cleaned.indexOf("Build info:");
        if (buildIdx > 0) cleaned = cleaned.substring(0, buildIdx);
        int sysIdx = cleaned.indexOf("System info:");
        if (sysIdx > 0) cleaned = cleaned.substring(0, sysIdx);
        int driverIdx = cleaned.indexOf("Driver info:");
        if (driverIdx > 0) cleaned = cleaned.substring(0, driverIdx);
        return cleaned.replaceAll("(\\r?\\n)+", " ").replaceAll("\\s+", " ").trim();
    }
}