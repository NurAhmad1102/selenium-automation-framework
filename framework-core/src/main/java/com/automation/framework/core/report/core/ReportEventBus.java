package com.automation.framework.core.report.core;

import com.automation.framework.core.report.internal.model.LogEntry;
import com.automation.framework.core.report.internal.model.ReportData;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Event bus to broadcast log events to all registered reporters.
 */
public final class ReportEventBus {

    private static final List<IAutomationReporter> reporters = new CopyOnWriteArrayList<>();

    private ReportEventBus() {
    }

    public static void register(IAutomationReporter reporter) {
        if (reporter != null && !reporters.contains(reporter)) {
            reporters.add(reporter);
        }
    }

    public static void clearReporters() {
        reporters.clear();
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
