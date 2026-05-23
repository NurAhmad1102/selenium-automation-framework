package org.example.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility to collect diagnostic information during a test execution.
 * These messages are only retrieved and logged if the test fails.
 */
public class DiagnosticCollector {

    private static final ThreadLocal<List<String>> diagnostics = ThreadLocal.withInitial(ArrayList::new);

    public static void addDiagnostic(String message) {
        diagnostics.get().add(message);
    }

    public static List<String> getAndClear() {
        List<String> current = new ArrayList<>(diagnostics.get());
        diagnostics.remove();
        return current;
    }

    public static void clear() {
        diagnostics.remove();
    }
}
