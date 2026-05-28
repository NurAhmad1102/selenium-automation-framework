package org.example.utils.extentReportUtils;

import org.example.listener.Listeners;
import org.example.utils.ExtentReportNg;
import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.implementations.extent.ExtentImplementation;
import org.testng.Assert;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;

public class ReportSkipTest {

    @Test
    public void testSkippedReportGeneration() throws IOException {
        // Initialize reporting engine
        ExtentImplementation.init();

        // 1. Simulate a skipped test (like in BaseTest)
        String testName = "MockSkippedTest";
        ReportAPI.startTest(testName, "Simulated skipped test");
        
        Exception skipReason = new RuntimeException("net::ERR_CONNECTION_TIMED_OUT");
        ReportAPI.skip("Test Skipped: connection timed out", skipReason);
        ReportAPI.flush();
        ExtentReportNg.getReportObject().flush();

        // 2. Locate the generated index.html report
        String reportPath = System.getProperty("user.dir") + File.separator + "reports" + File.separator + "index.html";
        File reportFile = new File(reportPath);
        
        Assert.assertTrue(reportFile.exists(), "Extent report file should be generated at: " + reportPath);

        // 3. Read the HTML file content to verify skip statuses are present
        String htmlContent = new String(Files.readAllBytes(Paths.get(reportPath)));

        // 4. Verify overall test status in report
        Assert.assertTrue(
            htmlContent.contains("skip-bg") || htmlContent.contains("status skip") || htmlContent.contains("badge-skip"),
            "Report should contain skipped indicator (skip-bg, status skip, or badge-skip)"
        );

        // 5. Verify detail step has the custom badge-skip class
        Assert.assertTrue(
            htmlContent.contains("badge-skip"),
            "Report CSS/HTML should contain the custom 'badge-skip' class"
        );
        
        System.out.println("Verification successful! Skips are properly recorded in the Extent Report.");
    }

    @Test
    public void testListenersOnTestSkippedDuplication() throws IOException {
        ExtentImplementation.init();
        Listeners listener = new Listeners();

        // 1. Simulate TestNG lifecycle for a skipped test where onTestStart was called
        String testName = "TestWithOnTestStartAndSkip";
        ITestResult result = createMockTestResult(testName, "Test Description", new RuntimeException("Setup Failed"));

        // Simulate onTestStart (which calls ReportAPI.startTest)
        listener.onTestStart(result);

        // Simulate onTestSkipped
        listener.onTestSkipped(result);

        // Flush report to disk
        ExtentReportNg.getReportObject().flush();

        // 2. Verify index.html content
        String reportPath = System.getProperty("user.dir") + File.separator + "reports" + File.separator + "index.html";
        String htmlContent = new String(Files.readAllBytes(Paths.get(reportPath)));

        String formattedName = ExtentReportNg.formatterTestName(testName);

        // 3. Scan the test-items in the report to ensure no duplicate test list entry is created
        int testItemCount = 0;
        int liIndex = 0;
        while ((liIndex = htmlContent.indexOf("test-item\"", liIndex)) != -1) {
            int nextLi = htmlContent.indexOf("test-item\"", liIndex + 10);
            if (nextLi == -1) {
                nextLi = htmlContent.length();
            }
            String liBlock = htmlContent.substring(liIndex, nextLi);
            if (liBlock.contains(formattedName)) {
                testItemCount++;
            }
            liIndex = nextLi;
        }

        Assert.assertEquals(testItemCount, 1, "There should be exactly 1 test-item entry for " + formattedName + " in the report");
        System.out.println("Duplication check successful! Only 1 test entry exists for the skipped test.");
    }

    private ITestResult createMockTestResult(String methodName, String description, Throwable throwable) {
        ITestNGMethod mockMethod = (ITestNGMethod) Proxy.newProxyInstance(
            ITestNGMethod.class.getClassLoader(),
            new Class<?>[]{ITestNGMethod.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getMethodName")) {
                    return methodName;
                }
                if (method.getName().equals("getDescription")) {
                    return description;
                }
                return null;
            }
        );

        return (ITestResult) Proxy.newProxyInstance(
            ITestResult.class.getClassLoader(),
            new Class<?>[]{ITestResult.class},
            (proxy, method, args) -> {
                if (method.getName().equals("getMethod")) {
                    return mockMethod;
                }
                if (method.getName().equals("getThrowable")) {
                    return throwable;
                }
                if (method.getName().equals("getAttribute")) {
                    return null; // reportManagedByBaseTest
                }
                return null;
            }
        );
    }

    @Test
    public void testSetupFailureClassification() {
        // Test infrastructure skip exception classification
        Exception connTimeout = new org.openqa.selenium.WebDriverException("unknown error: net::ERR_CONNECTION_TIMED_OUT");
        Assert.assertTrue(ReportAPI.isInfrastructureSkip(connTimeout), "WebDriverException with ERR_CONNECTION_TIMED_OUT must be classified as infrastructure skip");

        Exception sessionNotCreated = new org.openqa.selenium.SessionNotCreatedException("session not created");
        Assert.assertTrue(ReportAPI.isInfrastructureSkip(sessionNotCreated), "SessionNotCreatedException must be classified as infrastructure skip");

        Exception skipException = new org.testng.SkipException("Explicit testng skip");
        Assert.assertTrue(ReportAPI.isInfrastructureSkip(skipException), "org.testng.SkipException must be classified as infrastructure skip");

        // Test functional exception classification
        Exception noSuchElement = new org.openqa.selenium.NoSuchElementException("Unable to locate element: By.xpath: //a");
        Assert.assertFalse(ReportAPI.isInfrastructureSkip(noSuchElement), "NoSuchElementException must not be classified as infrastructure skip (should fail)");

        Throwable assertionError = new java.lang.AssertionError("expected [true] but found [false]");
        Assert.assertFalse(ReportAPI.isInfrastructureSkip(assertionError), "AssertionError must not be classified as infrastructure skip (should fail)");
    }
}
