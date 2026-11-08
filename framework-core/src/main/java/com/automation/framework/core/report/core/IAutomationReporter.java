package com.automation.framework.core.report.core;

import com.automation.framework.core.report.internal.model.LogEntry;
import com.automation.framework.core.report.internal.model.ReportData;

/**
 * Interface for different reporting engine implementations.
 */
public interface IAutomationReporter {

    void onLogAdded(LogEntry entry);

    void flush(ReportData data);

    void startTest(String testName, String description);
}
