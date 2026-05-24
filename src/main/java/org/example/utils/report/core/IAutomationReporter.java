package org.example.utils.report.core;

import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.ReportData;

/**
 * Interface for different reporting implementations.
 */
public interface IAutomationReporter {
    void onLogAdded(LogEntry entry);
    void flush(ReportData data);
    void startTest(String testName, String description);
}
