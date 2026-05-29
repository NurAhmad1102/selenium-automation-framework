package org.example.utils.report.implementations.extent;

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.example.utils.report.core.TestOutcomeResolver;
import org.example.utils.report.internal.model.LogCategory;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.OutcomeStatus;
import org.example.utils.report.internal.model.ReportData;
import org.example.utils.report.internal.model.TestOutcome;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Renders structured log data into HTML tabs for ExtentReports.
 */
public class ExtentTabRenderer {

    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("h:mm:ss a")
                         .withZone(ZoneId.systemDefault());

    private static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();

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
        
        StringBuilder table = new StringBuilder("<table class='report-table details-table'><thead><tr><th>STATUS</th><th>OUTCOME</th><th>TIMESTAMP</th><th>STEP DESCRIPTION</th></tr></thead><tbody>");
        for (LogEntry log : logs) {
            String badge = statusClass(log.getStatus());
            String outcomeBadge = outcomeClass(log.getOutcomeStatus());
            table.append("<tr>")
                 .append("<td><span class='").append(badge).append("'>").append(log.getStatus()).append("</span></td>")
                 .append("<td><span class='").append(outcomeBadge).append("'>").append(log.getOutcomeStatus()).append("</span>")
                 .append("<div class='outcome-category'>").append(escapeHtml(String.valueOf(log.getOutcomeCategory()))).append("</div></td>")
                 .append("<td>").append(TIME_FORMATTER.format(log.getTimestamp())).append("</td>")
                 .append("<td>").append(escapeHtml(log.getMessage()))
                 .append("</td>")
                 .append("</tr>");
        }
        table.append("</tbody></table>");
        return table.toString();
    }

    private static String renderElementsTable(List<LogEntry> logs) {
        if (logs.isEmpty()) return "<i>No element issues recorded.</i>";

        StringBuilder table = new StringBuilder("<table class='report-table'><thead><tr><th>STATUS</th><th>OUTCOME</th><th>TIMESTAMP</th><th>DETAILS</th></tr></thead><tbody>");
        for (LogEntry log : logs) {
            table.append("<tr>")
                 .append("<td><span class='").append(statusClass(log.getStatus())).append("'>").append(log.getStatus()).append("</span></td>")
                 .append("<td><span class='").append(outcomeClass(log.getOutcomeStatus())).append("'>").append(log.getOutcomeStatus()).append("</span>")
                 .append("<div class='outcome-category'>").append(escapeHtml(String.valueOf(log.getOutcomeCategory()))).append("</div></td>")
                 .append("<td>").append(TIME_FORMATTER.format(log.getTimestamp())).append("</td>")
                 .append("<td>")
                 .append(log.getAction() != null ? "<div><b>Action:</b> " + escapeHtml(log.getAction()) + "</div>" : "")
                 .append("<div><b>Potential Cause:</b> ").append(escapeHtml(log.getMessage())).append("</div>")
                 .append(log.getLocator() != null ? "<div><b>Element:</b> <code>" + escapeHtml(log.getLocator()) + "</code></div>" : "")
                 .append(renderFailedElement(log))
                 .append(log.getError() != null ? "<pre class='code-block'>" + escapeHtml(stackTraceToString(log.getError())) + "</pre>" : "")
                 .append(log.getScreenshotPath() != null ? "<div style='margin-top: 10px;'><img class='r-img' style='max-width: 300px; border: 1px solid #ccc; cursor: pointer;' src='" + escapeHtml(log.getScreenshotPath()) + "' onclick='window.open(this.src)' /></div>" : "")
                 .append("</td>")
                 .append("</tr>");
        }
        table.append("</tbody></table>");
        return table.toString();
    }

    private static String renderNetworkLogs(List<LogEntry> logs) {
        if (logs.isEmpty()) return "<i>No network traffic captured.</i>";

        StringBuilder html = new StringBuilder("<table class='report-table'><thead><tr><th>STATUS</th><th>OUTCOME</th><th>TIMESTAMP</th><th>NETWORK REQUEST DETAILS</th></tr></thead><tbody>");
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
                    // pretty-print JSON
                    formattedJson = jsonFormatter(formattedJson);
                } catch (Exception e) {
                    // If parsing fails, fall back to raw response
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

    private static String renderFailedElement(LogEntry log) {
        String failedElement = resolveFailedElement(log);
        if (failedElement == null || failedElement.isBlank()) {
            return "";
        }
        String action = log.getAction() != null && !log.getAction().isBlank()
                ? log.getAction()
                : "Unknown action";
        return "<div><b>Failed Element:</b> "
                + escapeHtml(action)
                + " - <code>"
                + escapeHtml(failedElement)
                + "</code></div>";
    }

    private static String resolveFailedElement(LogEntry log) {
        String failedElement = extractFailedElement(log.getError());
        if (failedElement != null && !failedElement.isBlank()) {
            return failedElement;
        }
        return log.getLocator();
    }

    private static String summarizeOutcomeReason(String reason) {
        if (reason == null || reason.isBlank()) {
            return "No summary available";
        }

        String normalized = reason.replace("\r\n", "\n").replace('\r', '\n').trim();
        int lineBreak = normalized.indexOf('\n');
        String summary = lineBreak >= 0 ? normalized.substring(0, lineBreak) : normalized;

        int maxLength = 220;
        if (summary.length() > maxLength) {
            return summary.substring(0, maxLength - 3) + "...";
        }
        return summary;
    }

    private static String extractFailedElement(Throwable error) {
        Throwable current = error;
        while (current != null) {
            String message = current.getMessage();
            String extracted = extractElementSnippet(message);
            if (extracted != null) {
                return extracted;
            }
            Throwable cause = current.getCause();
            current = cause != current ? cause : null;
        }
        return null;
    }

    private static String extractElementSnippet(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }

        int elementStart = message.indexOf("Element <");
        if (elementStart < 0) {
            elementStart = message.indexOf("element <");
        }
        if (elementStart < 0) {
            return null;
        }

        int tagStart = message.indexOf('<', elementStart);
        if (tagStart < 0) {
            return null;
        }

        int tagEnd = message.indexOf('>', tagStart);
        if (tagEnd < 0) {
            return null;
        }

        int closingEnd = message.indexOf("</", tagEnd);
        if (closingEnd >= 0) {
            int fullClose = message.indexOf('>', closingEnd);
            if (fullClose >= 0) {
                return message.substring(tagStart, fullClose + 1);
            }
        }

        return message.substring(tagStart, tagEnd + 1);
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

    private static String stackTraceToString(Throwable error) {
        StringBuilder builder = new StringBuilder(error.toString());
        for (StackTraceElement element : error.getStackTrace()) {
            builder.append(System.lineSeparator()).append("\tat ").append(element);
        }
        return builder.toString();
    }

    private static String jsonFormatter(String rawJson) {
        JsonElement element = JsonParser.parseString(rawJson);
        rawJson = GSON_PRETTY.toJson(element);
        return rawJson;
    }

}
