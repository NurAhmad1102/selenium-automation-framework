package com.automation.framework.core.report.implementations.extent;

import com.automation.framework.core.report.core.IAutomationReporter;
import com.automation.framework.core.report.core.ReportEventBus;
import com.automation.framework.core.report.core.TestOutcomeResolver;
import com.automation.framework.core.report.internal.model.LogEntry;
import com.automation.framework.core.report.internal.model.ReportData;
import com.automation.framework.core.report.internal.model.TestOutcome;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * ExtentReports implementation of the automation reporter.
 */
public class ExtentImplementation implements IAutomationReporter {

    private static final ThreadLocal<ExtentTest> test = new ThreadLocal<>();
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private final ExtentReports extent = ExtentReportNg.getReportObject();

    public static void init() {
        if (initialized.compareAndSet(false, true)) {
            ReportEventBus.register(new ExtentImplementation());
        }
    }

    public static ExtentTest getCurrentTest() {
        return test.get();
    }

    @Override
    public void startTest(String testName, String description) {
        String formattedName = ExtentReportNg.formatterTestName(testName);
        ExtentTest extentTest = extent.createTest(formattedName, description);
        test.set(extentTest);
    }

    @Override
    public void onLogAdded(LogEntry entry) {
        // Handled via structured flush
    }

    @Override
    public void flush(ReportData data) {
        ExtentTest currentTest = test.get();
        try {
            if (currentTest != null) {
                String renderedHtml = ExtentTabRenderer.render(data);
                currentTest.info(renderedHtml);

                // Attach screenshots
                for (LogEntry log : data.getLogs()) {
                    if (log.getScreenshotPath() != null) {
                        currentTest.addScreenCaptureFromPath(log.getScreenshotPath(), log.getMessage());
                    }
                }

                TestOutcome outcome = TestOutcomeResolver.resolve(data);
                if (outcome.getExtentStatus() == Status.FAIL) {
                    currentTest.fail(outcome.getStatus() + ": " + outcome.getReason());
                } else if (outcome.getExtentStatus() == Status.SKIP) {
                    currentTest.skip(outcome.getStatus() + ": " + outcome.getReason());
                } else if (outcome.getExtentStatus() == Status.PASS) {
                    currentTest.pass(outcome.getStatus() + ": " + outcome.getReason());
                } else {
                    currentTest.info(outcome.getStatus() + ": " + outcome.getReason());
                }
            }
        } finally {
            test.remove();
        }
    }
}
