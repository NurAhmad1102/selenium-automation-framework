package org.example.listener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import com.aventstack.extentreports.ExtentReports;
import org.example.drivers.DriverManager;
import org.example.utils.ExtentReportNg;
import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.core.ThrowableOutcomeClassifier;
import org.example.utils.report.implementations.extent.ExtentImplementation;
import org.example.utils.report.internal.model.OutcomeStatus;
import org.example.utils.report.internal.model.TestOutcome;
import org.example.utils.serviceUtils.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.testng.IExecutionListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class Listeners implements ITestListener, IExecutionListener {

    private static final Logger logger = LogManager.getLogger(Listeners.class);
    private final SeleniumUtils seleniumUtils = new SeleniumUtils();

    @Override
    public void onExecutionStart() {
        org.example.utils.BrowserProcessCleaner.registerShutdownHook();
        org.example.utils.BrowserProcessCleaner.cleanupOldBrowserProfiles();
    }

    @Override
    public void onExecutionFinish() {
        try {
            org.example.utils.BrowserProcessCleaner.cleanupCurrentJvmDescendants();
        } catch (Exception e) {
            logger.error("Error during JVM-local process cleanup in execution finish", e);
        }
    }

    @Override
    public void onStart(ITestContext context) {
        // Initialize reporting system
        ExtentImplementation.init();

        if (context != null && context.getSuite() != null) {
            ExtentReports extent = ExtentReportNg.getReportObject();
            extent.setSystemInfo("Suite Name", context.getSuite().getName());
            if (context.getCurrentXmlTest() != null) {
                extent.setSystemInfo("Test Name", context.getCurrentXmlTest().getName());
            }
            extent.setSystemInfo("User", System.getProperty("user.name"));
            extent.setSystemInfo("OS", System.getProperty("os.name"));
            extent.setSystemInfo("Java Version", System.getProperty("java.version"));
            extent.setSystemInfo("Java Home", System.getProperty("java.home"));
            extent.setSystemInfo("Java VM", System.getProperty("java.vm.name"));

            String browserParam = context.getSuite().getParameter("browser");
            if (browserParam != null) {
                extent.setSystemInfo("Browser", browserParam);
            }
            extent.setSystemInfo("-----","---------");
        }
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
        TestOutcome outcome = ThrowableOutcomeClassifier.classify(result.getThrowable());
        if (outcome.getStatus() == OutcomeStatus.BROKEN) {
            ReportAPI.broken("Test Execution Broken", result.getThrowable());
        } else if (outcome.getStatus() == OutcomeStatus.BLOCKED) {
            ReportAPI.blocked("Test Blocked", result.getThrowable());
        } else {
            ReportAPI.fail("Test Execution Failed", result.getThrowable());
        }

        WebDriver driver = DriverManager.getDriver();
        if (driver != null) {
            try {
                String screenShotPath = seleniumUtils.getScreenShotPath(result.getMethod().getMethodName(), driver);
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
        String methodName = result.getMethod().getMethodName();
        String testDesc = result.getMethod().getDescription();

        ThreadContext.put("testMethod", methodName);
        if (!ReportAPI.isTestActive()) {
            ReportAPI.startTest(methodName, testDesc);
        }

        Throwable throwable = result.getThrowable();
        String msg = throwable != null ? throwable.getMessage() : "No reason provided";
        TestOutcome outcome = ThrowableOutcomeClassifier.classify(throwable);
        if (outcome.getStatus() == OutcomeStatus.BROKEN) {
            ReportAPI.broken("Test Setup Broken: " + msg, throwable);
            attachSetupScreenshot(methodName);
        } else if (outcome.getStatus() == OutcomeStatus.BLOCKED) {
            ReportAPI.blocked("Test Blocked: " + msg, throwable);
        } else if (outcome.getStatus() == OutcomeStatus.SKIP) {
            ReportAPI.skip("Test Skipped: " + msg, throwable);
        } else {
            ReportAPI.fail("Test Setup Failed: " + msg, throwable);
            attachSetupScreenshot(methodName);
        }

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

    private void attachSetupScreenshot(String methodName) {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return;
        }
        try {
            String screenShotPath = seleniumUtils.getScreenShotPath(methodName, driver);
            ReportAPI.addScreenshot(screenShotPath);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot for failed setup test: {}", methodName, e);
        }
    }
}
