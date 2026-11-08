package com.automation.framework.core.assertion;

import com.automation.framework.core.driver.DriverManager;
import com.automation.framework.core.page.BasePage;
import com.automation.framework.core.report.core.ReportAPI;
import com.automation.framework.core.utils.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.testng.asserts.IAssert;
import org.testng.asserts.SoftAssert;

/**
 * Custom Soft Assertion utility that automatically captures screenshots,
 * tracks failed assertion fields, and logs structured element errors without halting test execution.
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
        }

        String locator = BasePage.getLastInteractedLocator();
        if (locator == null || locator.isBlank()) {
            locator = assertCommand.getMessage() != null ? assertCommand.getMessage() : "Field Mismatch";
        }

        ReportAPI.elementError("Soft Assertion Check", locator, ex, null, screenshotPath);

        super.onAssertFailure(assertCommand, ex);
    }
}
