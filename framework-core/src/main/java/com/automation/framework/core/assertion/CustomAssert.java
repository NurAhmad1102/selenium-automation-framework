package com.automation.framework.core.assertion;

import com.automation.framework.core.report.core.ReportAPI;
import com.automation.framework.core.report.internal.model.OutcomeCategory;
import org.testng.Assert;

/**
 * Custom Assertion utility that automatically records failed assertions to the structured
 * HTML test report with rich context before throwing an AssertionError.
 */
public final class CustomAssert {

    private CustomAssert() {
    }

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            ReportAPI.fail("Assertion Failed: " + message, OutcomeCategory.ASSERTION);
        }
        Assert.assertTrue(condition, message);
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        boolean equal = (actual == null && expected == null) || (actual != null && actual.equals(expected));
        if (!equal) {
            String fullMessage = String.format("Assertion Failed: %s (Expected: [%s], but found: [%s])",
                    message, expected, actual);
            ReportAPI.fail(fullMessage, OutcomeCategory.ASSERTION);
        }
        Assert.assertEquals(actual, expected, message);
    }

    public static void fail(String message) {
        ReportAPI.fail("Manual Failure: " + message, OutcomeCategory.ASSERTION);
        Assert.fail(message);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            ReportAPI.fail("Assertion Failed: " + message, OutcomeCategory.ASSERTION);
        }
        Assert.assertFalse(condition, message);
    }
}
