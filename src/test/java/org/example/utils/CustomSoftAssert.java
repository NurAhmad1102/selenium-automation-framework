package org.example.utils;

import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.internal.model.OutcomeCategory;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

/**
 * Custom Soft Assertion utility that automatically logs failures to ExtentReports
 * with detailed context when assertions fail.
 */
public class CustomSoftAssert extends SoftAssert {

    @Override
    public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : assertCommand.getMessage();
        ReportAPI.fail("Soft Assertion Failed: " + message, OutcomeCategory.ASSERTION);
        ReportAPI.elementError("soft_assertion", message, ex);
        super.onAssertFailure(assertCommand, ex);
    }
}
