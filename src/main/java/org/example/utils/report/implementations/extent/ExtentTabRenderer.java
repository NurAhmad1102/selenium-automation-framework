package org.example.utils.report.implementations.extent;

import com.aventstack.extentreports.Status;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.logging.log4j.core.util.JsonUtils;
import org.example.utils.report.internal.model.LogCategory;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.ReportData;

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
                .filter(log -> log.getStatus() == Status.FAIL)
                .findFirst();

        StringBuilder html = new StringBuilder();
        html.append("<div class='test-report-wrapper'>");
        
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
        
        StringBuilder table = new StringBuilder("<table class='report-table details-table'><thead><tr><th>STATUS</th><th>TIMESTAMP</th><th>STEP DESCRIPTION</th></tr></thead><tbody>");
        for (LogEntry log : logs) {
            String badge = statusClass(log.getStatus());
            table.append("<tr>")
                 .append("<td><span class='").append(badge).append("'>").append(log.getStatus()).append("</span></td>")
                 .append("<td>").append(TIME_FORMATTER.format(log.getTimestamp())).append("</td>")
                 .append("<td>").append(escapeHtml(log.getMessage()))
                 .append(log.getError() != null ? "<pre class='code-block'>" + escapeHtml(stackTraceToString(log.getError())) + "</pre>" : "")
                 .append("</td>")
                 .append("</tr>");
        }
        table.append("</tbody></table>");
        return table.toString();
    }

    private static String renderElementsTable(List<LogEntry> logs) {
        if (logs.isEmpty()) return "<i>No element issues recorded.</i>";

        StringBuilder table = new StringBuilder("<table class='report-table'><thead><tr><th>STATUS</th><th>TIMESTAMP</th><th>DETAILS</th></tr></thead><tbody>");
        for (LogEntry log : logs) {
            table.append("<tr>")
                 .append("<td><span class='").append(statusClass(log.getStatus())).append("'>").append(log.getStatus()).append("</span></td>")
                 .append("<td>").append(TIME_FORMATTER.format(log.getTimestamp())).append("</td>")
                 .append("<td>")
                 .append(log.getAction() != null ? "<div><b>Action:</b> " + escapeHtml(log.getAction()) + "</div>" : "")
                 .append("<div><b>Potential Cause:</b> ").append(escapeHtml(log.getMessage())).append("</div>")
                 .append(log.getLocator() != null ? "<div><b>Element:</b> <code>" + escapeHtml(log.getLocator()) + "</code></div>" : "")
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

        StringBuilder html = new StringBuilder("<table class='report-table'><thead><tr><th>STATUS</th><th>TIMESTAMP</th><th>NETWORK REQUEST DETAILS</th></tr></thead><tbody>");
        for (LogEntry log : logs) {
            int statusCode = resolveStatusCode(log);
            html.append("<tr>")
                .append("<td><span class='badge-info'>Info</span></td>")
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
