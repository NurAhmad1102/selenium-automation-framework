package org.example.utils;

import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.internal.model.OutcomeCategory;
import org.testng.Assert;

/**
 * Custom Assertion utility that automatically logs failures to ExtentReports
 * with detailed context before throwing the AssertionError.
 */
public class CustomAssert {

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            ReportAPI.fail("Assertion Failed: " + message, OutcomeCategory.ASSERTION);
            ReportAPI.elementError("assertion", message, new AssertionError(message));
        }
        Assert.assertTrue(condition, message);
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        boolean equal = (actual == null && expected == null) || (actual != null && actual.equals(expected));
        if (!equal) {
            String fullMessage = String.format("Assertion Failed: %s (Expected: [%s], but found: [%s])", 
                    message, expected, actual);
            ReportAPI.fail(fullMessage, OutcomeCategory.ASSERTION);
            ReportAPI.elementError("assertion", fullMessage, new AssertionError(fullMessage));
        }
        Assert.assertEquals(actual, expected, message);
    }

    public static void fail(String message) {
        ReportAPI.fail("Manual Failure: " + message, OutcomeCategory.ASSERTION);
        ReportAPI.elementError("assertion", message, new AssertionError(message));
        Assert.fail(message);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            ReportAPI.fail("Assertion Failed: " + message, OutcomeCategory.ASSERTION);
            ReportAPI.elementError("assertion", message, new AssertionError(message));
        }
        Assert.assertFalse(condition, message);
    }
}
