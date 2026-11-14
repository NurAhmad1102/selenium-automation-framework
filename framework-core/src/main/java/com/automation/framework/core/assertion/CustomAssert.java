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

    /**
     * Asserts that a condition is true. If false, logs a failure to ReportAPI and throws AssertionError.
     *
     * @param condition condition to check
     * @param message descriptive failure message
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            ReportAPI.fail("Assertion Failed: " + message, OutcomeCategory.ASSERTION);
        }
        Assert.assertTrue(condition, message);
    }

    /**
     * Asserts that two objects are equal. If not equal, logs details to ReportAPI and throws AssertionError.
     *
     * @param actual actual value observed
     * @param expected expected value
     * @param message descriptive failure message
     */
    public static void assertEquals(Object actual, Object expected, String message) {
        boolean equal = (actual == null && expected == null) || (actual != null && actual.equals(expected));
        if (!equal) {
            String fullMessage = String.format("Assertion Failed: %s (Expected: [%s], but found: [%s])",
                    message, expected, actual);
            ReportAPI.fail(fullMessage, OutcomeCategory.ASSERTION);
        }
        Assert.assertEquals(actual, expected, message);
    }

    /**
     * Fails a test unconditionally with the given message.
     *
     * @param message failure message
     */
    public static void fail(String message) {
        ReportAPI.fail("Manual Failure: " + message, OutcomeCategory.ASSERTION);
        Assert.fail(message);
    }

    /**
     * Asserts that a condition is false. If true, logs a failure to ReportAPI and throws AssertionError.
     *
     * @param condition condition to check
     * @param message descriptive failure message
     */
    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            ReportAPI.fail("Assertion Failed: " + message, OutcomeCategory.ASSERTION);
        }
        Assert.assertFalse(condition, message);
    }
}
