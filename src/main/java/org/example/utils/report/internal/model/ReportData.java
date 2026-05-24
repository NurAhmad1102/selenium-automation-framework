package org.example.utils.report.internal.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for logs belonging to a single test execution.
 */
public class ReportData {
    private final List<LogEntry> logs = new ArrayList<>();
    private boolean active;

    public void addLog(LogEntry entry) {
        logs.add(entry);
    }

    public List<LogEntry> getLogs() {
        return new ArrayList<>(logs);
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void clear() {
        logs.clear();
        active = false;
    }
}
