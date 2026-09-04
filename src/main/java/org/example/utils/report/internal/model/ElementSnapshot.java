package org.example.utils.report.internal.model;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates the 6 diagnostic pillars of an element at the time of failure:
 * 1. Locator Identity (Strategy & Resolved Value)
 * 2. State Snapshot (Flags, Bounding Box, Computed CSS)
 * 3. DOM Snippet (outerHTML, parent/context snippet)
 * 4. Execution Context & Wait Metadata (Action, Timeout, Elapsed, Retry)
 * 5. Intercept / Obstruction Details (Overlay element)
 * 6. Error Signature (Exception name, concise message)
 */
public class ElementSnapshot implements Serializable {

    // 1. Locator Identity
    private String locatorStrategy;
    private String locatorValue;
    private String resolvedLocator;

    // 2. State Snapshot
    private Boolean displayed;
    private Boolean enabled;
    private Boolean selected;
    private String boundingBox; // e.g., "x: 640, y: 480, w: 120, h: 36"
    private Map<String, String> computedStyles = new LinkedHashMap<>(); // visibility, display, opacity, pointer-events

    // 3. DOM Snippet
    private String outerHtml;
    private String parentContainerHtml;

    // 4. Execution Context & Wait Metadata
    private String actionType;
    private String waitTimeout;
    private String elapsedTime;
    private int retryCount;

    // 5. Intercept / Obstruction Details
    private String overlayElement;

    // 6. Error & Exception Signature
    private String exceptionName;
    private String conciseErrorMessage;

    public ElementSnapshot() {
    }

    // Getters and Setters
    public String getLocatorStrategy() { return locatorStrategy; }
    public void setLocatorStrategy(String locatorStrategy) { this.locatorStrategy = locatorStrategy; }

    public String getLocatorValue() { return locatorValue; }
    public void setLocatorValue(String locatorValue) { this.locatorValue = locatorValue; }

    public String getResolvedLocator() { return resolvedLocator; }
    public void setResolvedLocator(String resolvedLocator) { this.resolvedLocator = resolvedLocator; }

    public Boolean getDisplayed() { return displayed; }
    public void setDisplayed(Boolean displayed) { this.displayed = displayed; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }

    public String getBoundingBox() { return boundingBox; }
    public void setBoundingBox(String boundingBox) { this.boundingBox = boundingBox; }

    public Map<String, String> getComputedStyles() { return computedStyles; }
    public void setComputedStyles(Map<String, String> computedStyles) { this.computedStyles = computedStyles; }
    public void addComputedStyle(String key, String value) { this.computedStyles.put(key, value); }

    public String getOuterHtml() { return outerHtml; }
    public void setOuterHtml(String outerHtml) { this.outerHtml = outerHtml; }

    public String getParentContainerHtml() { return parentContainerHtml; }
    public void setParentContainerHtml(String parentContainerHtml) { this.parentContainerHtml = parentContainerHtml; }

    public String getActionType() { return actionType; }
    public void setActionType(String actionType) { this.actionType = actionType; }

    public String getWaitTimeout() { return waitTimeout; }
    public void setWaitTimeout(String waitTimeout) { this.waitTimeout = waitTimeout; }

    public String getElapsedTime() { return elapsedTime; }
    public void setElapsedTime(String elapsedTime) { this.elapsedTime = elapsedTime; }

    public int getRetryCount() { return retryCount; }
    public void setRetryCount(int retryCount) { this.retryCount = retryCount; }

    public String getOverlayElement() { return overlayElement; }
    public void setOverlayElement(String overlayElement) { this.overlayElement = overlayElement; }

    public String getExceptionName() { return exceptionName; }
    public void setExceptionName(String exceptionName) { this.exceptionName = exceptionName; }

    public String getConciseErrorMessage() { return conciseErrorMessage; }
    public void setConciseErrorMessage(String conciseErrorMessage) { this.conciseErrorMessage = conciseErrorMessage; }
}