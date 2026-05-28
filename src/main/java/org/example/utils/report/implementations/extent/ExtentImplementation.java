package org.example.utils.report.implementations.extent;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.example.utils.ExtentReportNg;
import org.example.utils.report.core.IAutomationReporter;
import org.example.utils.report.core.ReportEventBus;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.ReportData;

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
        // We can optionally log important events real-time to Extent
        // but for tabbed reporting, we mostly rely on the flush() method.
        if (entry.getStatus() == Status.FAIL) {
            // Optional: immediately mark test as failed in Extent UI
            // if (test.get() != null) test.get().fail(entry.getMessage());
        }
    }

    @Override
    public void flush(ReportData data) {
        ExtentTest currentTest = test.get();
        if (currentTest != null) {
            String renderedHtml = ExtentTabRenderer.render(data);
            currentTest.info(renderedHtml);
            
            // Attach screenshots
            for (LogEntry log : data.getLogs()) {
                if (log.getScreenshotPath() != null) {
                    currentTest.addScreenCaptureFromPath(log.getScreenshotPath(), log.getMessage());
                }
            }

            // Handle overall test status
            boolean hasFailures = data.getLogs().stream().anyMatch(l -> l.getStatus() == Status.FAIL);
            boolean hasSkips = data.getLogs().stream().anyMatch(l -> l.getStatus() == Status.SKIP);
            if (hasFailures) {
                currentTest.fail("Test failed. Check ELEMENT LOGS or NETWORK LOGS tabs for details.");
            } else if (hasSkips) {
                currentTest.skip("Test skipped.");
            } else {
                currentTest.pass("Test passed.");
            }
        }
        test.remove();
    }
}
