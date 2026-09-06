package org.example.utils.report;

import com.aventstack.extentreports.Status;
import org.example.utils.report.core.SeleniumErrorDiagnosticHelper;
import org.example.utils.report.implementations.extent.ExtentTabRenderer;
import org.example.utils.report.internal.model.LogCategory;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.OutcomeCategory;
import org.example.utils.report.internal.model.OutcomeStatus;
import org.example.utils.report.internal.model.ReportData;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.InvalidSelectorException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ExtentTabRendererTest {

    @Test
    public void testDetailsTableHasThreeColumnsAndHighlights() {
        ReportData data = new ReportData();
        LogEntry log1 = new LogEntry(LogCategory.DETAILS, Status.INFO, "Action: Click element [Submit Button]");
        log1.setOutcomeStatus(OutcomeStatus.INFO);
        log1.setOutcomeCategory(OutcomeCategory.LIFECYCLE);
        data.addLog(log1);

        LogEntry log2 = new LogEntry(LogCategory.DETAILS, Status.INFO, "Check: Locator [By.xpath: //div[contains(@class, 'border')]//h3[contains(normalize-space(), 'test')]] is visible");
        log2.setOutcomeStatus(OutcomeStatus.INFO);
        log2.setOutcomeCategory(OutcomeCategory.LIFECYCLE);
        data.addLog(log2);

        String html = ExtentTabRenderer.render(data);

        Assert.assertTrue(html.contains("STATUS / TYPE"), "Table should have unified STATUS / TYPE column");
        Assert.assertTrue(html.contains("STEP &amp; EXECUTION DETAILS"), "Table should have unified details column");
        Assert.assertTrue(html.contains("badge-action"), "Action badge should be present");
        Assert.assertTrue(html.contains("locator-badge"), "Locator badge should be present");
        // Verify no broken tags:
        Assert.assertFalse(html.contains("<code>By.xpath: //div<span"), "XPath brackets should not break into nested span inside code tag");
    }

    @Test
    public void testSoftAssertionFormattingCard() {
        ReportData data = new ReportData();
        String softAssertMessage = "Test Execution Failed: The following asserts failed: " +
                "New Position [null] harus ditampilkan di Flow 3 expected [true] but found [false], " +
                "Parent Position untuk [null] tidak sesuai Excel. Expected: [Network Sales Head], Actual: [] expected [Network Sales Head] but found []";
        
        LogEntry failLog = new LogEntry(LogCategory.DETAILS, Status.FAIL, softAssertMessage);
        failLog.setOutcomeStatus(OutcomeStatus.FAIL);
        failLog.setOutcomeCategory(OutcomeCategory.ASSERTION);
        data.addLog(failLog);

        String html = ExtentTabRenderer.render(data);

        Assert.assertTrue(html.contains("failure-detail-card"), "Failure detail card should be rendered");
        Assert.assertTrue(html.contains("SOFT ASSERT"), "SOFT ASSERT badge should be rendered");
        Assert.assertTrue(html.contains("diff-expected"), "Expected diff badge should be rendered");
        Assert.assertTrue(html.contains("diff-actual"), "Actual diff badge should be rendered");
        Assert.assertTrue(html.contains("Item 1"), "Item 1 should be rendered");
        Assert.assertTrue(html.contains("Item 2"), "Item 2 should be rendered");
    }

    @Test
    public void testDiagnosticHelperPatterns() {
        // 1. Null data
        AssertionError assertError = new AssertionError("New Position [null] expected [true] but found [false]");
        SeleniumErrorDiagnosticHelper.DiagnosticResult res1 =
                SeleniumErrorDiagnosticHelper.analyzeDiagnostic(assertError, "//h3", "Assertion");
        Assert.assertEquals(res1.getErrorTitle(), "Null / Empty Test Data");
        Assert.assertTrue(res1.getSuggestion().contains("Excel"));

        // 2. Click Intercepted
        ElementClickInterceptedException clickError = new ElementClickInterceptedException("Element <button> is not clickable at point (100, 100). Other element <div class='backdrop'> would receive the click");
        SeleniumErrorDiagnosticHelper.DiagnosticResult res2 =
                SeleniumErrorDiagnosticHelper.analyzeDiagnostic(clickError, "//button", "Click");
        Assert.assertEquals(res2.getErrorTitle(), "Element Click Intercepted");
        Assert.assertTrue(res2.getSuggestion().contains("overlay"));

        // 3. Stale element
        StaleElementReferenceException staleError = new StaleElementReferenceException("element is not attached to the page document");
        SeleniumErrorDiagnosticHelper.DiagnosticResult res3 =
                SeleniumErrorDiagnosticHelper.analyzeDiagnostic(staleError, "//input", "Type");
        Assert.assertEquals(res3.getErrorTitle(), "Stale Element Reference");

        // 4. Invalid xpath syntax
        InvalidSelectorException invalidXpath = new InvalidSelectorException("syntax error in xpath [//div[@class='test']");
        SeleniumErrorDiagnosticHelper.DiagnosticResult res4 =
                SeleniumErrorDiagnosticHelper.analyzeDiagnostic(invalidXpath, "//div", "Find");
        Assert.assertEquals(res4.getErrorTitle(), "Invalid Selector / XPath Syntax");

        // 5. Timeout
        TimeoutException timeoutError = new TimeoutException("Expected condition failed: waiting for visibility of element located by By.xpath: //div[@id='modal']");
        SeleniumErrorDiagnosticHelper.DiagnosticResult res5 =
                SeleniumErrorDiagnosticHelper.analyzeDiagnostic(timeoutError, "//div[@id='modal']", "Wait");
        Assert.assertEquals(res5.getErrorTitle(), "Element Timeout / Not Found");
    }

    @Test
    public void testElementLogsInvestigationHubRendering() {
        ReportData data = new ReportData();
        NoSuchElementException noSuch = new NoSuchElementException("Unable to locate element: //button[@id='submit']");
        LogEntry elementLog = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        elementLog.setAction("Click Element");
        elementLog.setLocator("//button[@id='submit']");
        elementLog.setError(noSuch);
        elementLog.setScreenshotPath("capture/20260903/error.png");
        data.addLog(elementLog);

        String html = ExtentTabRenderer.render(data);

        Assert.assertTrue(html.contains("dbg-card"), "Debug card should be present in HTML");
        Assert.assertTrue(html.contains("copyLocator"), "Copy locator button should be present");
        Assert.assertTrue(html.contains("dbg-badge-fail"), "Fail badge should be present");
        Assert.assertTrue(html.contains("Evidence"), "Screenshot evidence row should be present");
    }

    @Test
    public void testElementLogsSixPillarsSnapshotRendering() {
        ReportData data = new ReportData();
        org.openqa.selenium.ElementClickInterceptedException ex = new org.openqa.selenium.ElementClickInterceptedException(
                "element click intercepted: Element <button id='btn-save'> is not clickable at point (640, 480). Other element <div class='modal-backdrop'> would receive the click");

        org.example.utils.report.internal.model.ElementSnapshot snapshot = new org.example.utils.report.internal.model.ElementSnapshot();
        snapshot.setLocatorStrategy("XPath");
        snapshot.setLocatorValue("//button[@id='btn-save']");
        snapshot.setActionType("Click");
        snapshot.setWaitTimeout("15s");
        snapshot.setElapsedTime("15.1s");
        snapshot.setDisplayed(true);
        snapshot.setEnabled(true);
        snapshot.setSelected(false);
        snapshot.setBoundingBox("x: 640, y: 480, w: 120, h: 36");
        snapshot.addComputedStyle("visibility", "visible");
        snapshot.addComputedStyle("display", "block");
        snapshot.addComputedStyle("opacity", "1");
        snapshot.addComputedStyle("pointer-events", "auto");
        snapshot.setOuterHtml("<button id='btn-save' class='btn btn-primary'>Save</button>");
        snapshot.setOverlayElement("<div class='modal-backdrop'>");
        snapshot.setConciseErrorMessage("element click intercepted at point (640, 480)");

        LogEntry elementLog = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        elementLog.setAction("Click");
        elementLog.setLocator("//button[@id='btn-save']");
        elementLog.setError(ex);
        elementLog.setElementSnapshot(snapshot);
        elementLog.setScreenshotPath("capture/20260904/intercept.png");
        data.addLog(elementLog);

        String html = ExtentTabRenderer.render(data);

        Assert.assertTrue(html.contains("Locator Identity"), "Pillar 1: Locator identity section should be present");
        Assert.assertTrue(html.contains("btn-save"), "Target locator value should be present");
        Assert.assertTrue(html.contains("State Snapshot") && html.contains("Computed Styles"), "Pillar 2: State snapshot section should be present");
        Assert.assertTrue(html.contains("isDisplayed: true"), "isDisplayed flag should be present");
        Assert.assertTrue(html.contains("visibility"), "Computed styles should be present");
        Assert.assertTrue(html.contains("Intercept / Obstruction Details"), "Pillar 5: Intercept details section should be present");
        Assert.assertTrue(html.contains("modal-backdrop"), "Obstructing element should be present");
        Assert.assertTrue(html.contains("Target Element DOM (outerHTML)"), "Pillar 3: DOM snippet section should be present");
        Assert.assertTrue(html.contains("Error Signature"), "Pillar 6: Error signature section should be present");
    }

    @Test
    public void testNetworkLogsFixedLayoutRendering() {
        ReportData data = new ReportData();
        LogEntry netLog = new LogEntry(LogCategory.NETWORK_LOGS, Status.INFO, "GetMenu API - HTTP 401");
        netLog.setAction("GET");
        netLog.setLocator("http://localhost:5173/api/menu");
        netLog.setStatusCode(401);
        netLog.setStatusText("Unauthorized");
        netLog.setResponse("{\"code\":401,\"message\":\"Invalid JWT\"}");
        data.addLog(netLog);

        String html = ExtentTabRenderer.render(data);

        Assert.assertTrue(html.contains("network-table"), "Network table should have network-table class for fixed 4-column layout");
        Assert.assertTrue(html.contains("STATUS"), "STATUS column should be present");
        Assert.assertTrue(html.contains("NETWORK REQUEST DETAILS"), "NETWORK REQUEST DETAILS column should be present");
    }

    @Test
    public void testElementLogsSoftAssertFailureWithScreenshotRendering() {
        ReportData data = new ReportData();
        AssertionError error = new AssertionError("The following asserts failed:\n\tFTE pada New Position [Database Operation Testing - AHM] tidak sesuai Excel. Expected: [1], Actual: [8] expected [1] but found [8]");
        
        LogEntry softAssertLog = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        softAssertLog.setAction("Soft Assertion Check");
        softAssertLog.setLocator("By.xpath: //div[button[.//h3[normalize-space()='Database Operation Testing - AHM']]]//p[normalize-space()='FTE']/following-sibling::p[1]");
        softAssertLog.setError(error);
        softAssertLog.setScreenshotPath("capture/20260904/softAssert_fail.png");
        data.addLog(softAssertLog);

        String html = ExtentTabRenderer.render(data);

        Assert.assertTrue(html.contains("dbg-card"), "Debug card should be present in Element Logs");
        Assert.assertTrue(html.contains("Database Operation Testing - AHM"), "Failed target field should be extracted and displayed");
        Assert.assertTrue(html.contains("Target Locator"), "Target Locator row should be present");
        Assert.assertTrue(html.contains("following-sibling::p[1]"), "Target Locator value should be present");
        Assert.assertTrue(html.contains("dbg-expected"), "Expected badge should be present");
        Assert.assertTrue(html.contains("dbg-actual"), "Actual badge should be present");
        Assert.assertTrue(html.contains("softAssert_fail.png"), "Screenshot evidence link should be linked inside the card");
    }

    @Test
    public void testElementLogsDeduplication() {
        ReportData data = new ReportData();
        // Log 1: from CustomSoftAssert onAssertFailure (with screenshot and helper origin)
        AssertionError error1 = new AssertionError("FTE pada New Position [Database Operation Testing - AHM] tidak sesuai Excel. Expected: [90], Actual: [8]");
        LogEntry log1 = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        log1.setAction("Soft Assertion Check");
        log1.setError(error1);
        log1.setScreenshotPath("capture/20260904/softAssert_fail.png");
        data.addLog(log1);

        // Log 2: duplicate from assertAll() re-thrown
        AssertionError error2 = new AssertionError("The following asserts failed:\n\tFTE pada New Position [Database Operation Testing - AHM] tidak sesuai Excel. Expected: [90], Actual: [8] expected [90] but found [8]");
        LogEntry log2 = new LogEntry(LogCategory.ELEMENT_LOGS, Status.FAIL, "Element Error Occurred");
        log2.setAction("Assertion Validation");
        log2.setError(error2);
        data.addLog(log2);

        // Log 3: orphan screenshot log from listener
        LogEntry orphanScreenshot = new LogEntry(LogCategory.ELEMENT_LOGS, Status.INFO, "Screenshot captured");
        orphanScreenshot.setScreenshotPath("capture/20260904/softAssert_fail.png");
        data.addLog(orphanScreenshot);

        String html = ExtentTabRenderer.render(data);

        // Should ONLY have #1 FAIL, not #2 FAIL
        Assert.assertTrue(html.contains("#1 FAIL"), "#1 FAIL should be present");
        Assert.assertFalse(html.contains("#2 FAIL"), "#2 FAIL should NOT be present because it's a duplicate");
        Assert.assertFalse(html.contains("Screenshot captured ("), "Orphan screenshot card should NOT be rendered when failure card has screenshot");
    }
}
