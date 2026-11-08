package com.automation.framework.core.report.internal.storage;

import com.automation.framework.core.report.internal.model.ReportData;

/**
 * Thread-safe storage for per-test report data.
 */
public final class ThreadLocalStore {

    private static final ThreadLocal<ReportData> reportStore = ThreadLocal.withInitial(ReportData::new);

    private ThreadLocalStore() {
    }

    public static ReportData get() {
        return reportStore.get();
    }

    public static void clear() {
        reportStore.get().clear();
        reportStore.remove();
    }
}
