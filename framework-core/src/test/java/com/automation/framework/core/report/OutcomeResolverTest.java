package com.automation.framework.core.report;

import com.automation.framework.core.report.core.TestOutcomeResolver;
import com.automation.framework.core.report.internal.model.LogCategory;
import com.automation.framework.core.report.internal.model.LogEntry;
import com.automation.framework.core.report.internal.model.OutcomeStatus;
import com.automation.framework.core.report.internal.model.TestOutcome;
import com.aventstack.extentreports.Status;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class OutcomeResolverTest {

    @Test
    public void testResolvePassWhenAllPass() {
        LogEntry step = new LogEntry(LogCategory.DETAILS, Status.PASS, "Step 1 passed");
        TestOutcome outcome = TestOutcomeResolver.resolve(List.of(step));

        Assert.assertEquals(outcome.getStatus(), OutcomeStatus.PASS);
    }

    @Test
    public void testResolveFailWhenAnyFailed() {
        LogEntry passLog = new LogEntry(LogCategory.DETAILS, Status.PASS, "Step 1 passed");
        LogEntry failLog = new LogEntry(LogCategory.DETAILS, Status.FAIL, "Step 2 failed");

        TestOutcome outcome = TestOutcomeResolver.resolve(List.of(passLog, failLog));
        Assert.assertEquals(outcome.getStatus(), OutcomeStatus.FAIL);
    }
}
