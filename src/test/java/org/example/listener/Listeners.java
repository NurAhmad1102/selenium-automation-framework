package org.example.listener;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.drivers.DriverManager;
import org.example.utils.ExtentReportNg;
import org.example.utils.serviceUtils.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.IOException;

public class Listeners extends SeleniumUtils implements ITestListener {

    private static final Logger logger = LogManager.getLogger(Listeners.class);
    private static ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    ExtentReports extent = ExtentReportNg.getReportObject();

    @Override
    public void onTestStart(ITestResult result) {

        String methodName = result.getMethod().getMethodName();
        String testDesc = result.getMethod().getDescription();
        String testName = ExtentReportNg.formatterTestName(methodName);


        test.set( extent.createTest(
                testName,
                "<b>Description:</b><br>" + testDesc));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (test.get() != null) {
            test.get().log(Status.PASS, "Test Passed");
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (test.get() != null) {
            test.get().fail(result.getThrowable());
        }

        WebDriver driver= DriverManager.getDriver();
        if(driver != null){
            try {
                String screenShotPath = getScreenShotPath(result.getMethod().getMethodName(), driver);
                if (test.get() != null) {
                    test.get().addScreenCaptureFromPath(screenShotPath, result.getMethod().getMethodName());
                }

            } catch (Exception e) {
                logger.error("Failed to capture screenshot for failed test: {}",
                        result.getMethod().getMethodName(), e);
            }

        } else {
            logger.warn("Driver is null, screenshot cannot be captured for failed test: {}",
                    result.getMethod().getMethodName());
        }

    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (test.get() != null) {
            test.get().log(Status.SKIP, "Test Skipped");

            Throwable skipReason = result.getThrowable();
            if(skipReason != null){
                test.get().skip(skipReason);
            }
        }
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

    }

    @Override
    public void onTestFailedWithTimeout(ITestResult result) {

    }

    @Override
    public void onStart(ITestContext context) {

    }

    @Override
    public void onFinish(ITestContext context) {
        extent.flush();
    }

//    @Override
//    public boolean isEnabled() {
//        return false;
//    }

}
