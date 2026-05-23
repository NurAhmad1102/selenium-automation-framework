package org.example.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.example.listener.Listeners;
import org.testng.Assert;

/**
 * Custom Assertion utility that automatically logs failures to ExtentReports
 * with detailed context before throwing the AssertionError.
 */
public class CustomAssert {

    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            logFailure(message);
        }
        Assert.assertTrue(condition, message);
    }

    public static void assertEquals(Object actual, Object expected, String message) {
        boolean equal = (actual == null && expected == null) || (actual != null && actual.equals(expected));
        if (!equal) {
            String fullMessage = String.format("%s (Expected: [%s], but found: [%s])", 
                    message, expected, actual);
            logFailure(fullMessage);
        }
        Assert.assertEquals(actual, expected, message);
    }

    public static void fail(String message) {
        logFailure(message);
        Assert.fail(message);
    }

    public static void assertFalse(boolean condition, String message) {
        if (condition) {
            logFailure(message);
        }
        Assert.assertFalse(condition, message);
    }

    private static void logFailure(String message) {
        ExtentTest test = Listeners.getExtentTest();
        if (test != null) {
            test.log(Status.FAIL, "<b>Assertion Failed:</b> " + message);
        }
    }
}
