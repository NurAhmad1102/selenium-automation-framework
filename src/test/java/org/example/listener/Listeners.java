package org.example.listener;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.example.drivers.DriverManager;
import org.example.utils.ExtentReportNg;
import org.example.utils.serviceUtils.SeleniumUtils;
import org.example.utils.DiagnosticCollector;
import org.openqa.selenium.WebDriver;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.util.List;

public class Listeners extends SeleniumUtils implements ITestListener {

    private static final Logger logger = LogManager.getLogger(Listeners.class);
    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static final ThreadLocal<String> currentTestMethod = new ThreadLocal<>();
    private static final ExtentReports extent = ExtentReportNg.getReportObject();

    public static ExtentTest getExtentTest() {
        return test.get();
    }

    public static void startTest(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        if (test.get() != null && methodName.equals(currentTestMethod.get())) {
            return;
        }

        ThreadContext.clearAll();
        DiagnosticCollector.clear();
        ThreadContext.put("testMethod", methodName);

        String testDesc = result.getMethod().getDescription();
        String testName = ExtentReportNg.formatterTestName(methodName);

        test.set(extent.createTest(
                testName,
                "<b>Description:</b><br>" + testDesc));
        currentTestMethod.set(methodName);
    }

    public static void clearCurrentTest() {
        test.remove();
        currentTestMethod.remove();
        ThreadContext.clearAll();
        DiagnosticCollector.clear();
    }

    @Override
    public void onTestStart(ITestResult result) {
        startTest(result);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        if (test.get() != null) {
            test.get().log(Status.PASS, "Test Passed");
        }
        clearCurrentTestIfNotManagedByBaseTest(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        if (test.get() != null) {
            test.get().fail(result.getThrowable());
            
            // Log collected diagnostics as FAIL
            List<String> diagnostics = DiagnosticCollector.getAndClear();
            for (String diagnostic : diagnostics) {
                test.get().log(Status.FAIL, "<b>Potential Cause:</b> " + diagnostic);
            }
        }

        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
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
        clearCurrentTestIfNotManagedByBaseTest(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        if (test.get() != null) {
            test.get().log(Status.SKIP, "Test Skipped");

            Throwable skipReason = result.getThrowable();
            if (skipReason != null) {
                test.get().skip(skipReason);
            }
        }
        clearCurrentTestIfNotManagedByBaseTest(result);
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

    private void clearCurrentTestIfNotManagedByBaseTest(ITestResult result) {
        if (!Boolean.TRUE.equals(result.getAttribute("extentManagedByBaseTest"))) {
            clearCurrentTest();
        }
    }
}
