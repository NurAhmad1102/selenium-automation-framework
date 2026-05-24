package org.example.utils.report.core;

import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.ReportData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Dispatcher to broadcast log events to all registered reporters.
 */
public class ReportEventBus {
    private static final List<IAutomationReporter> reporters = new CopyOnWriteArrayList<>();

    public static void register(IAutomationReporter reporter) {
        reporters.add(reporter);
    }

    public static void postLog(LogEntry entry) {
        for (IAutomationReporter reporter : reporters) {
            reporter.onLogAdded(entry);
        }
    }

    public static void startTest(String testName, String description) {
        for (IAutomationReporter reporter : reporters) {
            reporter.startTest(testName, description);
        }
    }

    public static void flush(ReportData data) {
        for (IAutomationReporter reporter : reporters) {
            reporter.flush(data);
        }
    }
}
