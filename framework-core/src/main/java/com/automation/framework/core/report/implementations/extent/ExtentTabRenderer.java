package com.automation.framework.core.report.implementations.extent;

import com.automation.framework.core.report.core.SeleniumErrorDiagnosticHelper;
import com.automation.framework.core.report.core.TestOutcomeResolver;
import com.automation.framework.core.report.internal.model.LogCategory;
import com.automation.framework.core.report.internal.model.LogEntry;
import com.automation.framework.core.report.internal.model.OutcomeStatus;
import com.automation.framework.core.report.internal.model.ReportData;
import com.automation.framework.core.report.internal.model.TestOutcome;
import com.aventstack.extentreports.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Renders structured log data into responsive HTML tabs for ExtentReports.
 */
public final class ExtentTabRenderer {

    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("h:mm:ss a")
                    .withZone(ZoneId.systemDefault());

    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

    private ExtentTabRenderer() {
    }

    public static String render(ReportData data) {
        String uniqueId = UUID.randomUUID().toString().substring(0, 8);
        List<LogEntry> allLogs = data.getLogs().stream()
                .sorted(Comparator.comparing(LogEntry::getTimestamp))
                .collect(Collectors.toList());
        Optional<LogEntry> firstFailure = allLogs.stream()
                .filter(log -> log.getOutcomeStatus() == OutcomeStatus.FAIL || log.getOutcomeStatus() == OutcomeStatus.BROKEN)
                .findFirst();
        TestOutcome outcome = TestOutcomeResolver.resolve(allLogs);

        StringBuilder html = new StringBuilder();
        html.append("<div class='test-report-wrapper'>");
        html.append(renderOutcomeSummary(outcome));

        // Tab Headers
        html.append("<div class='tab-container'>");
        html.append("<div class='tab-btn active' onclick=\"switchTab(event, 'details-").append(uniqueId).append("')\">DETAILS</div>");
        html.append("<div class='tab-btn' onclick=\"switchTab(event, 'elements-").append(uniqueId).append("')\">ELEMENT LOGS</div>");
        html.append("<div class='tab-btn' onclick=\"switchTab(event, 'network-").append(uniqueId).append("')\">NETWORK LOGS</div>");
        html.append("</div>");

        // Tab Content: DETAILS
        html.append("<div id='details-").append(uniqueId).append("' class='tab-panel active'>");
        html.append(renderDetailsTable(allLogs.stream()
                .filter(log -> log.getCategory() == LogCategory.DETAILS)
                .collect(Collectors.toList())));
        html.append("</div>");

        // Tab Content: ELEMENT LOGS
        html.append("<div id='elements-").append(uniqueId).append("' class='tab-panel'>");
        html.append(renderElementsTable(allLogs.stream()
                .filter(log -> log.getCategory() == LogCategory.ELEMENT_LOGS)
                .collect(Collectors.toList())));
        html.append("</div>");

        // Tab Content: NETWORK LOGS
        html.append("<div id='network-").append(uniqueId).append("' class='tab-panel'>");
        html.append(renderNetworkLogs(allLogs.stream()
                .filter(log -> log.getCategory() == LogCategory.NETWORK_LOGS)
                .filter(ExtentTabRenderer::isFailedNetworkLog)
                .filter(log -> firstFailure
                        .map(failure -> !log.getTimestamp().isAfter(failure.getTimestamp()))
                        .orElse(true))
                .collect(Collectors.toList())));
        html.append("</div>");

        html.append("</div>");
        return html.toString();
    }

    private static String renderDetailsTable(List<LogEntry> logs) {
        if (logs.isEmpty()) return "<i>No detail logs recorded.</i>";

        StringBuilder table = new StringBuilder("<table class='report-table details-table'><thead><tr><th>STATUS / TYPE</th><th>TIME</th><th>STEP &amp; EXECUTION DETAILS</th></tr></thead><tbody>");
        for (LogEntry log : logs) {
            String badge = statusClass(log.getStatus());
            table.append("<tr>")
                    .append("<td><span class='").append(badge).append("'>").append(log.getStatus()).append("</span>")
                    .append("<div class='outcome-category'>").append(escapeHtml(String.valueOf(log.getOutcomeCategory()))).append("</div></td>")
                    .append("<td>").append(TIME_FORMATTER.format(log.getTimestamp())).append("</td>")
                    .append("<td>").append(formatHighlightedStepDescription(log.getMessage())).append("</td>")
                    .append("</tr>");
        }
        table.append("</tbody></table>");
        return table.toString();
    }

    private static String renderElementsTable(List<LogEntry> logs) {
        if (logs.isEmpty()) {
            return "<div class='clean-state-box'><i>No element issues recorded.</i></div>";
        }

        List<LogEntry> uniqueLogs = new ArrayList<>();
        Set<String> seenSignatures = new HashSet<>();
        boolean hasFailures = false;

        for (LogEntry log : logs) {
            if (log.getError() != null || log.getStatus() == Status.FAIL) {
                hasFailures = true;
                String msg = log.getError() != null && log.getError().getMessage() != null
                        ? log.getError().getMessage()
                        : log.getMessage();
                String targetField = extractTargetField(msg);
                String exp = extractExpectedVal(msg);
                String act = extractActualVal(msg);

                String signature;
                if (targetField != null) {
                    signature = "ASSERT:" + targetField + "|" + exp + "|" + act;
                } else if (log.getLocator() != null) {
                    signature = "LOC:" + log.getLocator() + "|" + (log.getError() != null ? log.getError().getClass().getSimpleName() : "");
                } else {
                    signature = "ERR:" + (log.getError() != null ? log.getError().getMessage() : log.getMessage());
                }

                if (seenSignatures.add(signature)) {
                    uniqueLogs.add(log);
                }
            } else if (log.getScreenshotPath() != null && !log.getScreenshotPath().isBlank()) {
                uniqueLogs.add(log);
            }
        }

        if (hasFailures) {
            Set<String> failureScreenshots = uniqueLogs.stream()
                    .filter(l -> l.getError() != null || l.getStatus() == Status.FAIL)
                    .map(LogEntry::getScreenshotPath)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            uniqueLogs.removeIf(l -> (l.getError() == null && l.getStatus() != Status.FAIL && l.getScreenshotPath() != null
                    && (!failureScreenshots.isEmpty() || failureScreenshots.contains(l.getScreenshotPath()))));
        }

        if (uniqueLogs.isEmpty()) {
            return "<div class='clean-state-box'><i>No element issues recorded.</i></div>";
        }

        StringBuilder html = new StringBuilder("<div class='element-logs-container'>");

        long failureCount = uniqueLogs.stream().filter(l -> l.getError() != null || l.getStatus() == Status.FAIL).count();
        if (failureCount > 1) {
            html.append("<div class='dbg-summary-bar'>")
                    .append("<span><b>").append(failureCount).append(" Issues Recorded</b> in this execution:</span>")
                    .append("</div>");
        }

        int issueIndex = 1;
        for (LogEntry log : uniqueLogs) {
            if (log.getError() != null || log.getStatus() == Status.FAIL) {
                SeleniumErrorDiagnosticHelper.DiagnosticResult diag =
                        SeleniumErrorDiagnosticHelper.analyzeDiagnostic(log.getError(), log.getLocator(), log.getAction());

                String errorTitle = diag.getErrorTitle();
                boolean isAssertion = log.getError() instanceof AssertionError
                        || (log.getMessage() != null && log.getMessage().contains("Assertion"))
                        || errorTitle.contains("Assertion")
                        || errorTitle.contains("Data");

                String origin = diag.getOriginLine() != null && !diag.getOriginLine().isBlank()
                        ? diag.getOriginLine()
                        : "Unknown origin";

                html.append("<div class='dbg-card'>")
                        .append("<div class='dbg-header'>")
                        .append("<div>")
                        .append("<span class='dbg-badge-fail'>#").append(issueIndex).append(" FAIL</span> ")
                        .append("<b class='dbg-title'>").append(escapeHtml(errorTitle)).append("</b>")
                        .append("<span class='dbg-sep'>&bull;</span>")
                        .append("<span class='dbg-origin'>").append(escapeHtml(origin)).append("</span>")
                        .append("</div>")
                        .append("<div class='dbg-time'>").append(TIME_FORMATTER.format(log.getTimestamp())).append("</div>")
                        .append("</div>");

                html.append("<div class='dbg-table'>");

                com.automation.framework.core.report.internal.model.ElementSnapshot snap = log.getElementSnapshot();
                if (snap != null) {
                    // 1. Locator Identity
                    html.append("<div class='dbg-section-title'>Locator Identity</div>");
                    html.append("<div class='dbg-row'>")
                            .append("<div class='dbg-key'>Strategy & Value</div>")
                            .append("<div class='dbg-val'>")
                            .append("<span class='dbg-flag'>").append(escapeHtml(snap.getLocatorStrategy() != null ? snap.getLocatorStrategy() : "XPath")).append("</span> ")
                            .append("<code class='locator-badge'>").append(escapeHtml(snap.getLocatorValue() != null ? snap.getLocatorValue() : log.getLocator())).append("</code>")
                            .append("<button class='copy-locator-btn' onclick=\"copyLocator(this, '").append(escapeJsString(snap.getLocatorValue() != null ? snap.getLocatorValue() : log.getLocator())).append("')\">Copy</button>")
                            .append("</div></div>");

                    if (snap.getResolvedLocator() != null && !snap.getResolvedLocator().equals(snap.getLocatorValue())) {
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Resolved Locator</div>")
                                .append("<div class='dbg-val'><code>").append(escapeHtml(snap.getResolvedLocator())).append("</code></div>")
                                .append("</div>");
                    }

                    // 2. Execution Context & Wait Metadata
                    html.append("<div class='dbg-row'>")
                            .append("<div class='dbg-key'>Action Context</div>")
                            .append("<div class='dbg-val'>")
                            .append("<b>").append(escapeHtml(snap.getActionType() != null ? snap.getActionType() : log.getAction())).append("</b>")
                            .append(snap.getWaitTimeout() != null ? " &bull; Wait Timeout: <code>" + escapeHtml(snap.getWaitTimeout()) + "</code>" : "")
                            .append(snap.getElapsedTime() != null ? " &bull; Elapsed: <code>" + escapeHtml(snap.getElapsedTime()) + "</code>" : "")
                            .append("</div></div>");

                    // 3. State Snapshot & Computed CSS
                    html.append("<div class='dbg-section-title'>State Snapshot & Computed Styles</div>");
                    html.append("<div class='dbg-row'>")
                            .append("<div class='dbg-key'>Element Flags</div>")
                            .append("<div class='dbg-val'>")
                            .append("<span class='dbg-flag ").append(Boolean.TRUE.equals(snap.getDisplayed()) ? "dbg-flag-true" : "dbg-flag-false").append("'>isDisplayed: ").append(snap.getDisplayed()).append("</span>")
                            .append("<span class='dbg-flag ").append(Boolean.TRUE.equals(snap.getEnabled()) ? "dbg-flag-true" : "dbg-flag-false").append("'>isEnabled: ").append(snap.getEnabled()).append("</span>")
                            .append("<span class='dbg-flag ").append(Boolean.TRUE.equals(snap.getSelected()) ? "dbg-flag-true" : "").append("'>isSelected: ").append(snap.getSelected()).append("</span>")
                            .append("</div></div>");

                    if (snap.getBoundingBox() != null) {
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Bounding Box</div>")
                                .append("<div class='dbg-val'><code>").append(escapeHtml(snap.getBoundingBox())).append("</code></div>")
                                .append("</div>");
                    }

                    if (snap.getComputedStyles() != null && !snap.getComputedStyles().isEmpty()) {
                        StringBuilder cssSb = new StringBuilder();
                        snap.getComputedStyles().forEach((k, v) -> cssSb.append(k).append(": <b>").append(escapeHtml(v)).append("</b> &bull; "));
                        String cssStr = cssSb.toString();
                        if (cssStr.endsWith(" &bull; ")) cssStr = cssStr.substring(0, cssStr.length() - 8);
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Computed CSS</div>")
                                .append("<div class='dbg-val'>").append(cssStr).append("</div>")
                                .append("</div>");
                    }

                    // 4. Intercept / Obstruction Details
                    if (snap.getOverlayElement() != null && !snap.getOverlayElement().isBlank()) {
                        html.append("<div class='dbg-section-title'>⚠️ Intercept / Obstruction Details</div>");
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Obstructed By</div>")
                                .append("<div class='dbg-val' style='color:#b91c1c; font-weight:700;'><code>").append(escapeHtml(snap.getOverlayElement())).append("</code></div>")
                                .append("</div>");
                    }

                    // 5. DOM Snippet
                    if (snap.getOuterHtml() != null && !snap.getOuterHtml().isBlank()) {
                        html.append("<div class='dbg-section-title'>Target Element DOM (outerHTML)</div>");
                        html.append("<div class='dom-snippet-pre'>").append(escapeHtml(snap.getOuterHtml())).append("</div>");
                    } else if (snap.getParentContainerHtml() != null && !snap.getParentContainerHtml().isBlank()) {
                        html.append("<div class='dbg-section-title'>Parent / Modal Context</div>");
                        html.append("<div class='dom-snippet-pre'>").append(escapeHtml(snap.getParentContainerHtml())).append("</div>");
                    }

                    // 6. Error Signature
                    if (snap.getConciseErrorMessage() != null && !snap.getConciseErrorMessage().isBlank()) {
                        html.append("<div class='dbg-section-title'>Error Signature</div>");
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Error Message</div>")
                                .append("<div class='dbg-val' style='color:#991b1b; font-family:system-ui, sans-serif;'>").append(escapeHtml(snap.getConciseErrorMessage())).append("</div>")
                                .append("</div>");
                    }
                } else if (isAssertion) {
                    String msg = log.getError() != null && log.getError().getMessage() != null
                            ? log.getError().getMessage()
                            : log.getMessage();

                    if (msg != null && (msg.contains("The following asserts failed:") || msg.contains("\n\t"))) {
                        String[] items = msg.split("(\\r?\\n\\t|\\r?\\n(?=\\d+\\)))");
                        boolean firstItem = true;
                        int itemNum = 1;
                        for (String item : items) {
                            String trimmed = item.trim();
                            if (trimmed.startsWith("The following asserts failed:")) {
                                trimmed = trimmed.substring(29).trim();
                            }
                            if (trimmed.isBlank()) continue;

                            String targetField = extractTargetField(trimmed);
                            String expectedVal = extractExpectedVal(trimmed);
                            String actualVal = extractActualVal(trimmed);

                            if (!firstItem) {
                                html.append("<div style='border-top: 1px dashed #e2e8f0; margin: 6px 0; padding-top: 6px;'></div>");
                            }
                            html.append("<div class='dbg-row'>")
                                    .append("<div class='dbg-key'>Item #").append(itemNum).append("</div>")
                                    .append("<div class='dbg-val' style='font-weight:700;'>").append(escapeHtml(targetField != null ? targetField : trimmed)).append("</div>")
                                    .append("</div>");

                            if (expectedVal != null) {
                                html.append("<div class='dbg-row'>")
                                        .append("<div class='dbg-key'>Expected</div>")
                                        .append("<div class='dbg-val'><span class='dbg-expected'>").append(escapeHtml(expectedVal)).append("</span></div>")
                                        .append("</div>");
                            }
                            if (actualVal != null) {
                                html.append("<div class='dbg-row'>")
                                        .append("<div class='dbg-key'>Actual</div>")
                                        .append("<div class='dbg-val'><span class='dbg-actual'>").append(escapeHtml(actualVal.isBlank() ? "\"\" (Empty)" : actualVal)).append("</span></div>")
                                        .append("</div>");
                            }
                            firstItem = false;
                            itemNum++;
                        }
                    } else {
                        String targetField = extractTargetField(msg);
                        String expectedVal = extractExpectedVal(msg);
                        String actualVal = extractActualVal(msg);

                        if (targetField != null) {
                            html.append("<div class='dbg-row'>")
                                    .append("<div class='dbg-key'>Target Field</div>")
                                    .append("<div class='dbg-val' style='font-weight:700;'>").append(escapeHtml(targetField)).append("</div>")
                                    .append("</div>");
                        }
                        if (expectedVal != null) {
                            html.append("<div class='dbg-row'>")
                                    .append("<div class='dbg-key'>Expected</div>")
                                    .append("<div class='dbg-val'><span class='dbg-expected'>").append(escapeHtml(expectedVal)).append("</span></div>")
                                    .append("</div>");
                        }
                        if (actualVal != null) {
                            html.append("<div class='dbg-row'>")
                                    .append("<div class='dbg-key'>Actual</div>")
                                    .append("<div class='dbg-val'><span class='dbg-actual'>").append(escapeHtml(actualVal.isBlank() ? "\"\" (Empty)" : actualVal)).append("</span></div>")
                                    .append("</div>");
                        }
                    }

                    String locator = log.getLocator();
                    if (locator != null && !locator.isBlank() && !locator.startsWith("Assertion:") && !locator.equals("Data Assertion") && !locator.equals("Field Mismatch") && !locator.equals("Unknown Locator")) {
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Target Locator</div>")
                                .append("<div class='dbg-val'>")
                                .append("<code class='locator-badge'>").append(escapeHtml(locator)).append("</code>")
                                .append("<button class='copy-locator-btn' onclick=\"copyLocator(this, '").append(escapeJsString(locator)).append("')\">Copy</button>")
                                .append("</div>")
                                .append("</div>");
                    }

                    if (log.getAction() != null && !log.getAction().isBlank()) {
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Action Method</div>")
                                .append("<div class='dbg-val'><code>").append(escapeHtml(log.getAction())).append("</code></div>")
                                .append("</div>");
                    }
                } else {
                    String locator = log.getLocator();
                    if (locator != null && !locator.isBlank() && !locator.startsWith("Assertion")) {
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Target Locator</div>")
                                .append("<div class='dbg-val'>")
                                .append("<code class='locator-badge'>").append(escapeHtml(locator)).append("</code>")
                                .append("<button class='copy-locator-btn' onclick=\"copyLocator(this, '").append(escapeJsString(locator)).append("')\">Copy</button>")
                                .append("</div>")
                                .append("</div>");
                    }

                    html.append("<div class='dbg-row'>")
                            .append("<div class='dbg-key'>Root Cause</div>")
                            .append("<div class='dbg-val' style='font-family:system-ui, sans-serif;'>").append(escapeHtml(diag.getPotentialCause())).append("</div>")
                            .append("</div>");

                    if (log.getAction() != null && !log.getAction().isBlank()) {
                        html.append("<div class='dbg-row'>")
                                .append("<div class='dbg-key'>Action Method</div>")
                                .append("<div class='dbg-val'><code>").append(escapeHtml(log.getAction())).append("</code></div>")
                                .append("</div>");
                    }
                }

                if (log.getScreenshotPath() != null && !log.getScreenshotPath().isBlank()) {
                    html.append("<div class='dbg-row'>")
                            .append("<div class='dbg-key'>Evidence</div>")
                            .append("<div class='dbg-val'>")
                            .append("<a href='javascript:void(0)' onclick=\"window.open('").append(escapeHtml(log.getScreenshotPath())).append("')\" class='dbg-screenshot-link'>📸 View Screenshot Snapshot</a>")
                            .append("</div>")
                            .append("</div>");
                }

                html.append("</div>"); // close dbg-table

                if (log.getError() != null) {
                    html.append(renderStackTrace(log.getError()));
                }

                html.append("</div>"); // close dbg-card
                issueIndex++;
            } else if (log.getScreenshotPath() != null && !log.getScreenshotPath().isBlank()) {
                html.append("<div class='dbg-card' style='padding:10px 14px;'>")
                        .append("<div style='font-size:11.5px; font-weight:700; margin-bottom:6px;'>📸 ").append(escapeHtml(log.getMessage())).append(" (").append(TIME_FORMATTER.format(log.getTimestamp())).append(")</div>")
                        .append("<div style='max-width:240px;'><img class='r-img' style='width:100%; border-radius:4px; border:1px solid #cbd5e1; cursor:pointer;' src='").append(escapeHtml(log.getScreenshotPath())).append("' onclick='window.open(this.src)' /></div>")
                        .append("</div>");
            }
        }

        html.append("</div>");
        return html.toString();
    }

    private static String renderNetworkLogs(List<LogEntry> logs) {
        if (logs.isEmpty()) return "<div class='clean-state-box'><i>No network traffic captured.</i></div>";

        StringBuilder html = new StringBuilder("<table class='report-table network-table'><thead><tr><th>STATUS</th><th>OUTCOME</th><th>TIMESTAMP</th><th>NETWORK REQUEST DETAILS</th></tr></thead><tbody>");
        for (LogEntry log : logs) {
            int statusCode = resolveStatusCode(log);
            html.append("<tr>")
                    .append("<td><span class='badge-info'>Info</span></td>")
                    .append("<td><span class='").append(outcomeClass(log.getOutcomeStatus())).append("'>").append(log.getOutcomeStatus()).append("</span>")
                    .append("<div class='outcome-category'>").append(escapeHtml(String.valueOf(log.getOutcomeCategory()))).append("</div></td>")
                    .append("<td>").append(TIME_FORMATTER.format(log.getTimestamp())).append("</td>")
                    .append("<td>")
                    .append("<div style='margin-bottom:10px;'><b>")
                    .append(escapeHtml(resolveNetworkName(log))).append(" API - <span style='color:red'>HTTP ")
                    .append(statusCode).append("</span> - occurred ")
                    .append(resolveOccurrenceCount(log)).append(" times</b></div>")
                    .append("<div><span class='network-detail-label'>Name:</span> ").append(escapeHtml(resolveNetworkName(log))).append("</div>")
                    .append("<div><span class='network-detail-label'>Method:</span> ").append(escapeHtml(log.getAction())).append("</div>")
                    .append("<div style='word-break: break-all;'><span class='network-detail-label'>URL:</span> ")
                    .append(escapeHtml(log.getLocator())).append("</div>")
                    .append("<div><span class='network-detail-label'>Status:</span> ")
                    .append(statusCode)
                    .append(log.getStatusText() != null && !log.getStatusText().isBlank() ? " " + escapeHtml(log.getStatusText()) : "")
                    .append("</div>");

            if (log.getMimeType() != null && !log.getMimeType().isBlank()) {
                html.append("<div><span class='network-detail-label'>Mime Type:</span> ")
                        .append(escapeHtml(log.getMimeType()))
                        .append("</div>");
            }

            if (log.getRequest() != null && !log.getRequest().isBlank()) {
                String formattedRequest = jsonFormatter(log.getRequest());

                html.append("<div style='margin-top:10px;'><b>Request Payload:</b></div>")
                        .append("<pre class='code-block'>")
                        .append(escapeHtml(formattedRequest))
                        .append("</pre>");
            }

            if (log.getResponse() != null && !log.getResponse().isBlank()) {
                String formattedJson = log.getResponse();
                try {
                    formattedJson = jsonFormatter(formattedJson);
                } catch (Exception ignored) {
                }

                html.append("<div style='margin-top:10px;'><b>Response Body:</b></div>")
                        .append("<pre class='code-block'>")
                        .append(escapeHtml(formattedJson))
                        .append("</pre>");
            }

            html.append("</td>")
                    .append("</tr>");
        }
        html.append("</tbody></table>");
        return html.toString();
    }

    private static int resolveStatusCode(LogEntry log) {
        return log.getStatusCode() > 0 ? log.getStatusCode() : log.getRetryCount();
    }

    private static int resolveOccurrenceCount(LogEntry log) {
        return log.getOccurrenceCount() > 0 ? log.getOccurrenceCount() : 1;
    }

    private static String resolveNetworkName(LogEntry log) {
        if (log.getName() != null && !log.getName().isBlank()) {
            return log.getName();
        }
        String message = log.getMessage();
        if (message != null && message.endsWith(" API - HTTP " + resolveStatusCode(log))) {
            return message.substring(0, message.indexOf(" API - HTTP "));
        }
        return "Unknown";
    }

    private static boolean isFailedNetworkLog(LogEntry log) {
        int statusCode = resolveStatusCode(log);
        return statusCode >= 400 && statusCode <= 599;
    }

    private static String statusClass(Status status) {
        if (status == Status.FAIL) {
            return "badge-fail";
        }
        if (status == Status.PASS) {
            return "badge-pass";
        }
        if (status == Status.SKIP) {
            return "badge-skip";
        }
        return "badge-info";
    }

    private static String outcomeClass(OutcomeStatus status) {
        if (status == OutcomeStatus.BROKEN) {
            return "badge-broken";
        }
        if (status == OutcomeStatus.BLOCKED) {
            return "badge-blocked";
        }
        if (status == OutcomeStatus.FAIL) {
            return "badge-fail";
        }
        if (status == OutcomeStatus.PASS) {
            return "badge-pass";
        }
        if (status == OutcomeStatus.SKIP) {
            return "badge-skip";
        }
        if (status == OutcomeStatus.WARNING) {
            return "badge-warning";
        }
        return "badge-info";
    }

    private static String renderOutcomeSummary(TestOutcome outcome) {
        return "<div class='outcome-summary'>"
                + "<span class='" + outcomeClass(outcome.getStatus()) + "'>" + outcome.getStatus() + "</span>"
                + "<span class='outcome-summary-text'>"
                + escapeHtml(summarizeOutcomeReason(outcome.getReason()))
                + "</span></div>";
    }

    private static String summarizeOutcomeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "No summary available";
        }

        String cleaned = cleanFailureMessageForDetails(reason);
        String normalized = cleaned.replace("\r\n", "\n").replace('\r', '\n').trim();
        int lineBreak = normalized.indexOf('\n');
        String summary = lineBreak >= 0 ? normalized.substring(0, lineBreak) : normalized;

        int maxLength = 220;
        if (summary.length() > maxLength) {
            return summary.substring(0, maxLength - 3) + "...";
        }
        return summary;
    }

    private static String formatHighlightedStepDescription(String rawMessage) {
        if (rawMessage == null || rawMessage.isBlank()) {
            return "";
        }

        String cleaned = cleanFailureMessageForDetails(rawMessage);

        if (cleaned.contains("The following asserts failed:") || cleaned.startsWith("Soft Assertion Check Failed:")) {
            return formatSoftAssertionCard(cleaned);
        }

        if ((cleaned.contains("Failed to ") || cleaned.contains("Test Execution Failed")) && (cleaned.contains("Reason:") || cleaned.contains("Locator Detail:"))) {
            return formatFailureCard(cleaned);
        }

        return highlightStepText(cleaned);
    }

    private static String formatSoftAssertionCard(String cleaned) {
        String content = cleaned;
        if (content.startsWith("Soft Assertion Check Failed: ")) {
            content = content.substring(29).trim();
        }
        if (content.startsWith("Test Execution Failed: ")) {
            content = content.substring(23).trim();
        }
        if (content.startsWith("The following asserts failed: ")) {
            content = content.substring(30).trim();
        }

        String[] items = content.split("(?<=expected\\s\\[[^\\]]*\\]\\sbut\\sfound\\s\\[[^\\]]*\\]),\\s*");
        if (items.length == 1) {
            items = content.split("(?<=\\]),\\s*(?=[A-Z])");
        }

        StringBuilder card = new StringBuilder("<div class='failure-detail-card'>");
        card.append("<div class='failure-header'>")
                .append("<span>⚠️ Validation Failures (").append(items.length).append(" Assertions Failed)</span>")
                .append("<span style='font-size:10px; background:rgba(0,0,0,0.2); padding:2px 6px; border-radius:4px;'>SOFT ASSERT</span>")
                .append("</div>")
                .append("<div class='soft-assert-container'>");

        for (int i = 0; i < items.length; i++) {
            String item = items[i].trim();
            if (item.isBlank()) continue;

            String expectedVal = null;
            String actualVal = null;
            String desc = item;

            int expIdx = item.indexOf("expected [");
            if (expIdx >= 0) {
                int expEnd = item.indexOf("] but found [", expIdx);
                if (expEnd >= 0) {
                    expectedVal = item.substring(expIdx + 10, expEnd);
                    int actEnd = item.indexOf(']', expEnd + 13);
                    if (actEnd >= 0) {
                        actualVal = item.substring(expEnd + 13, actEnd);
                    }
                    desc = item.substring(0, expIdx).trim();
                }
            } else if (item.contains("Expected: [") && item.contains("Actual: [")) {
                int expStart = item.indexOf("Expected: [");
                int expClose = item.indexOf(']', expStart);
                int actStart = item.indexOf("Actual: [");
                int actClose = item.indexOf(']', actStart);
                if (expStart >= 0 && expClose > expStart) {
                    expectedVal = item.substring(expStart + 11, expClose);
                }
                if (actStart >= 0 && actClose > actStart) {
                    actualVal = item.substring(actStart + 9, actClose);
                }
                desc = item.substring(0, expStart).trim();
            }

            card.append("<div class='soft-assert-item'>")
                    .append("<div class='soft-assert-title'>")
                    .append("<span class='soft-assert-bullet'></span>")
                    .append("<span>Item ").append(i + 1).append("</span>")
                    .append("</div>")
                    .append("<div class='soft-assert-desc'>").append(highlightStepText(desc)).append("</div>");

            if (expectedVal != null || actualVal != null) {
                card.append("<div class='soft-assert-diff'>")
                        .append("<span class='diff-expected'>Expected: ").append(escapeHtml(expectedVal != null ? expectedVal : "-")).append("</span>")
                        .append("<span class='diff-arrow'>&rarr;</span>")
                        .append("<span class='diff-actual'>Actual: ").append(escapeHtml(actualVal != null && !actualVal.isBlank() ? actualVal : "\"\" (Empty)")).append("</span>")
                        .append("</div>");
            }

            card.append("</div>");
        }

        card.append("</div></div>");
        return card.toString();
    }

    private static String formatFailureCard(String cleaned) {
        String title = "Test Execution Failed";
        String locator = null;
        String reason = null;

        String temp = cleaned;
        if (temp.startsWith("Test Execution Failed: ")) {
            temp = temp.substring(23).trim();
        }

        int reasonIdx = temp.indexOf(". Reason: ");
        if (reasonIdx < 0) {
            reasonIdx = temp.indexOf(" Reason: ");
        }

        int locatorDetailIdx = temp.indexOf(". Locator Detail: ");
        if (locatorDetailIdx < 0) {
            locatorDetailIdx = temp.indexOf(" Locator Detail: ");
        }

        if (reasonIdx >= 0) {
            reason = temp.substring(reasonIdx + 9).trim();
            if (reason.startsWith("Reason: ")) {
                reason = reason.substring(8).trim();
            }
            if (locatorDetailIdx >= 0 && locatorDetailIdx < reasonIdx) {
                title = temp.substring(0, locatorDetailIdx).trim();
                locator = temp.substring(locatorDetailIdx + 18, reasonIdx).trim();
            } else {
                title = temp.substring(0, reasonIdx).trim();
            }
        } else {
            title = temp;
        }

        if (locator != null && locator.startsWith("Locator: ")) {
            locator = locator.substring(9).trim();
        }

        StringBuilder card = new StringBuilder("<div class='failure-detail-card'>");
        card.append("<div class='failure-header'>")
                .append("<span>").append(escapeHtml(title)).append("</span>")
                .append("<span style='font-size:10px; background:rgba(0,0,0,0.2); padding:2px 6px; border-radius:4px;'>FAILURE</span>")
                .append("</div>")
                .append("<div style='padding:10px 12px;'>");

        if (reason != null && !reason.isBlank()) {
            card.append("<div style='color:#334155; font-size:12px; margin-bottom:6px;'><b>Reason:</b> ").append(escapeHtml(reason)).append("</div>");
        }

        if (locator != null && !locator.isBlank()) {
            card.append("<div style='color:#64748b; font-size:11.5px;'><b>Locator:</b> <code class='locator-badge'>").append(escapeHtml(locator)).append("</code></div>");
        }
        card.append("</div></div>");
        return card.toString();
    }

    private static String highlightStepText(String text) {
        if (text == null || text.isBlank()) {
            return "";
        }

        String raw = text;
        String prefix = "";
        if (raw.startsWith("Action: ")) {
            prefix = "<span class='badge-action'>ACTION</span> ";
            raw = raw.substring(8);
        } else if (raw.startsWith("Check: ")) {
            prefix = "<span class='badge-check'>CHECK</span> ";
            raw = raw.substring(7);
        }

        String htmlEscaped = escapeHtml(raw);

        String highlighted = htmlEscaped.replaceAll("&#39;(.*?)&#39;", "<span class='highlight-value'>$1</span>");
        highlighted = highlighted.replaceAll("\\[(By\\.[^:\\]]+:[^\\]]+)\\]", "<code class='locator-badge'>$1</code>");
        highlighted = highlighted.replaceAll("\\[(xpath:[^\\]]+)\\]", "<code class='locator-badge'>$1</code>");
        highlighted = highlighted.replaceAll("\\[([^\\[\\]]+)\\]", "<span class='highlight-element'>$1</span>");

        return prefix + highlighted;
    }

    private static String cleanFailureMessageForDetails(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }
        String cleaned = message;
        int buildInfoIdx = cleaned.indexOf("Build info:");
        if (buildInfoIdx >= 0) {
            cleaned = cleaned.substring(0, buildInfoIdx);
        }
        int sysInfoIdx = cleaned.indexOf("System info:");
        if (sysInfoIdx >= 0) {
            cleaned = cleaned.substring(0, sysInfoIdx);
        }
        int driverInfoIdx = cleaned.indexOf("Driver info:");
        if (driverInfoIdx >= 0) {
            cleaned = cleaned.substring(0, driverInfoIdx);
        }
        int capabilitiesIdx = cleaned.indexOf("Capabilities {");
        if (capabilitiesIdx >= 0) {
            cleaned = cleaned.substring(0, capabilitiesIdx);
        }
        int sessionIdx = cleaned.indexOf("Session ID:");
        if (sessionIdx >= 0) {
            cleaned = cleaned.substring(0, sessionIdx);
        }
        return cleaned.replaceAll("(\\r?\\n)+", " ").replaceAll("\\s+", " ").trim();
    }

    private static String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static String escapeJsString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\").replace("'", "\\'").replace("\"", "\\\"");
    }

    private static String renderStackTrace(Throwable error) {
        if (error == null) return "";

        String rawStackTrace = stackTraceToString(error);
        String formattedHtml = formatFormattedStackTrace(error);

        return "<details class='stacktrace-details'>"
                + "<summary class='stacktrace-summary'>"
                + "<span>Full Stack Trace</span>"
                + "<button class='copy-stacktrace-btn' type='button' onclick=\"event.stopPropagation(); copyStackTrace(this, '" + escapeJsString(rawStackTrace) + "')\">Copy Stack Trace</button>"
                + "</summary>"
                + "<div class='stacktrace-container'>"
                + "<pre class='code-block'>" + formattedHtml + "</pre>"
                + "</div>"
                + "</details>";
    }

    private static String formatFormattedStackTrace(Throwable error) {
        if (error == null) return "";
        StringBuilder sb = new StringBuilder();
        String errorHeader = error.toString();
        sb.append("<div class='st-header'>").append(escapeHtml(errorHeader)).append("</div>");

        for (StackTraceElement elem : error.getStackTrace()) {
            String line = "\tat " + elem.toString();
            String className = elem.getClassName();
            if (SeleniumErrorDiagnosticHelper.isApplicationFrame(className)) {
                sb.append("<div class='st-app-frame'>").append(escapeHtml(line)).append("</div>");
            } else {
                sb.append("<div class='st-sys-frame'>").append(escapeHtml(line)).append("</div>");
            }
        }
        return sb.toString();
    }

    private static String stackTraceToString(Throwable error) {
        StringBuilder builder = new StringBuilder(error.toString());
        for (StackTraceElement element : error.getStackTrace()) {
            builder.append(System.lineSeparator()).append("\tat ").append(element);
        }
        return builder.toString();
    }

    private static String extractTargetField(String msg) {
        if (msg == null) return null;
        int expIdx = msg.indexOf("Expected: [");
        if (expIdx > 0) {
            String field = msg.substring(0, expIdx).trim();
            if (field.startsWith("The following asserts failed:")) field = field.substring(29).trim();
            if (field.startsWith("Assertion Failed: ")) field = field.substring(18).trim();
            field = field.replaceFirst("^\\d+\\)\\s*", "").trim();
            if (field.endsWith(".")) field = field.substring(0, field.length() - 1).trim();
            return field.isBlank() ? null : field;
        }
        int expFoundIdx = msg.indexOf(" expected [");
        if (expFoundIdx > 0) {
            String field = msg.substring(0, expFoundIdx).trim();
            if (field.startsWith("The following asserts failed:")) field = field.substring(29).trim();
            if (field.startsWith("Assertion Failed: ")) field = field.substring(18).trim();
            field = field.replaceFirst("^\\d+\\)\\s*", "").trim();
            return field.isBlank() ? null : field;
        }
        return null;
    }

    private static String extractExpectedVal(String msg) {
        if (msg == null) return null;
        int start = msg.indexOf("Expected: [");
        if (start >= 0) {
            int end = msg.indexOf(']', start + 11);
            if (end > start) return msg.substring(start + 11, end);
        }
        int expFound = msg.indexOf(" expected [");
        if (expFound >= 0) {
            int butFound = msg.indexOf("] but found [", expFound);
            if (butFound > expFound) return msg.substring(expFound + 11, butFound);
        }
        return null;
    }

    private static String extractActualVal(String msg) {
        if (msg == null) return null;
        int start = msg.indexOf("Actual: [");
        if (start >= 0) {
            int end = msg.indexOf(']', start + 9);
            if (end > start) return msg.substring(start + 9, end);
        }
        int butFound = msg.indexOf("] but found [");
        if (butFound >= 0) {
            int end = msg.indexOf(']', butFound + 13);
            if (end > butFound) return msg.substring(butFound + 13, end);
        }
        return null;
    }

    private static String jsonFormatter(String rawJson) {
        JsonElement element = JsonParser.parseString(rawJson);
        return GSON_PRETTY.toJson(element);
    }
}
