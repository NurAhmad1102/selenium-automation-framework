package org.example.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.example.drivers.DriverManager;
import org.example.utils.ExtentReportNg;
import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.implementations.extent.ExtentImplementation;
import org.example.utils.serviceUtils.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class Listeners extends SeleniumUtils implements ITestListener {

    private static final Logger logger = LogManager.getLogger(Listeners.class);

    @Override
    public void onStart(ITestContext context) {
        // Initialize reporting system
        ExtentImplementation.init();
    }

    @Override
    public void onTestStart(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String testDesc = result.getMethod().getDescription();
        
        ThreadContext.put("testMethod", methodName);
        ReportAPI.startTest(methodName, testDesc);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (isManagedByBaseTest(result)) {
            return;
        }
        ReportAPI.pass("Test Execution Finished Successfully");
        ReportAPI.flush();
        ThreadContext.remove("testMethod");
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (isManagedByBaseTest(result)) {
            return;
        }
        ReportAPI.fail("Test Execution Failed", result.getThrowable());

        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            try {
                String screenShotPath = getScreenShotPath(result.getMethod().getMethodName(), driver);
                ReportAPI.addScreenshot(screenShotPath);
            } catch (Exception e) {
                logger.error("Failed to capture screenshot for failed test: {}",
                        result.getMethod().getMethodName(), e);
            }
        }
        
        ReportAPI.flush();
        ThreadContext.remove("testMethod");
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (isManagedByBaseTest(result)) {
            return;
        }
        ReportAPI.step("Test Skipped: " + result.getThrowable().getMessage());
        ReportAPI.flush();
        ThreadContext.remove("testMethod");
    }

    @Override
    public void onFinish(ITestContext context) {
        ExtentReportNg.getReportObject().flush();
    }
    
    // Remaining methods from interface
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onTestFailedWithTimeout(ITestResult result) {}

    private boolean isManagedByBaseTest(ITestResult result) {
        return Boolean.TRUE.equals(result.getAttribute("reportManagedByBaseTest"));
    }
}
