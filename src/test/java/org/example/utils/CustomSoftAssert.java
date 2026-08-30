package org.example.utils;

import org.example.drivers.DriverManager;
import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.internal.model.OutcomeCategory;
import org.example.utils.serviceUtils.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

/**
 * Custom Soft Assertion utility that automatically logs failures to ExtentReports
 * with detailed context and instant screenshot when assertions fail.
 */
public class CustomSoftAssert extends SoftAssert {

    private final SeleniumUtils seleniumUtils = new SeleniumUtils();

    @Override
    public void onAssertFailure(IAssert<?> assertCommand, AssertionError ex) {
        String message = ex.getMessage() != null ? ex.getMessage() : assertCommand.getMessage();
        ReportAPI.step("Soft Assertion Check Failed: " + message);
        ReportAPI.elementError("soft_assertion", message, ex);

        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver != null) {
                String screenshotPath = seleniumUtils.getScreenShotPath("softAssert_fail", driver);
                ReportAPI.addScreenshot(screenshotPath);
            }
        } catch (Exception ignored) {
            // Ignore screenshot failure to prevent overriding original assertion error
        }

        super.onAssertFailure(assertCommand, ex);
    }
}
