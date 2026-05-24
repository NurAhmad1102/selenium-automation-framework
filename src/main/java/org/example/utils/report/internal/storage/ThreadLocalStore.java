package org.example.utils.report.internal.storage;

import org.example.utils.report.internal.model.ReportData;

/**
 * Thread-safe storage for report data.
 */
public class ThreadLocalStore {
    private static final ThreadLocal<ReportData> reportStore = ThreadLocal.withInitial(ReportData::new);

    public static ReportData get() {
        return reportStore.get();
    }

    public static void clear() {
        reportStore.get().clear();
        reportStore.remove();
    }
}
