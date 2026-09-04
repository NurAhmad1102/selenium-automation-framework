package org.example.utils;

import org.example.drivers.DriverManager;
import org.example.utils.report.core.ReportAPI;
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

        String screenshotPath = null;
        try {
            WebDriver driver = DriverManager.getDriver();
            if (driver != null) {
                screenshotPath = seleniumUtils.getScreenShotPath("softAssert_fail", driver);
            }
        } catch (Exception ignored) {
            // Ignore screenshot failure to prevent overriding original assertion error
        }

        String locator = org.example.pageObject.base.BasePage.getLastInteractedLocator();
        if (locator == null || locator.isBlank()) {
            locator = assertCommand.getMessage() != null ? assertCommand.getMessage() : "Field Mismatch";
        }

        ReportAPI.elementError("Soft Assertion Check", locator, ex, null, screenshotPath);

        super.onAssertFailure(assertCommand, ex);
    }
}

