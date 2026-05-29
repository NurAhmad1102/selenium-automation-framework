package org.example.utils.report.core;

import com.aventstack.extentreports.Status;
import org.example.utils.report.internal.model.LogCategory;
import org.example.utils.report.internal.model.LogEntry;
import org.example.utils.report.internal.model.OutcomeCategory;
import org.example.utils.report.internal.model.OutcomeStatus;
import org.example.utils.report.internal.model.ReportData;
import org.example.utils.report.internal.model.TestOutcome;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TestOutcomeResolverTest {

    @Test
    public void brokenHasHigherPrecedenceThanFail() {
        ReportData data = new ReportData();
        data.addLog(outcomeLog(Status.FAIL, OutcomeStatus.FAIL, OutcomeCategory.ASSERTION, true));
        data.addLog(outcomeLog(Status.FAIL, OutcomeStatus.BROKEN, OutcomeCategory.INFRASTRUCTURE, true));

        TestOutcome outcome = TestOutcomeResolver.resolve(data);

        Assert.assertEquals(outcome.getStatus(), OutcomeStatus.BROKEN);
        Assert.assertEquals(outcome.getCategory(), OutcomeCategory.INFRASTRUCTURE);
    }

    @Test
    public void networkDiagnosticDoesNotOverridePass() {
        ReportData data = new ReportData();
        data.addLog(outcomeLog(Status.INFO, OutcomeStatus.INFO, OutcomeCategory.NETWORK, false));
        data.addLog(outcomeLog(Status.PASS, OutcomeStatus.PASS, OutcomeCategory.LIFECYCLE, true));

        TestOutcome outcome = TestOutcomeResolver.resolve(data);

        Assert.assertEquals(outcome.getStatus(), OutcomeStatus.PASS);
    }

    @Test
    public void resolverUsesOnlyGivenReportDataForParallelSafety() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<OutcomeStatus> passTask = () -> {
                ReportData data = new ReportData();
                data.addLog(outcomeLog(Status.PASS, OutcomeStatus.PASS, OutcomeCategory.LIFECYCLE, true));
                return TestOutcomeResolver.resolve(data).getStatus();
            };
            Callable<OutcomeStatus> brokenTask = () -> {
                ReportData data = new ReportData();
                data.addLog(outcomeLog(Status.FAIL, OutcomeStatus.BROKEN, OutcomeCategory.INFRASTRUCTURE, true));
                return TestOutcomeResolver.resolve(data).getStatus();
            };

            Future<OutcomeStatus> passResult = executor.submit(passTask);
            Future<OutcomeStatus> brokenResult = executor.submit(brokenTask);

            Assert.assertEquals(passResult.get(), OutcomeStatus.PASS);
            Assert.assertEquals(brokenResult.get(), OutcomeStatus.BROKEN);
        } finally {
            executor.shutdownNow();
        }
    }

    private LogEntry outcomeLog(Status extentStatus,
                                OutcomeStatus outcomeStatus,
                                OutcomeCategory outcomeCategory,
                                boolean affectsFinalOutcome) {
        LogEntry log = new LogEntry(LogCategory.DETAILS, extentStatus, outcomeStatus.name());
        log.setOutcomeStatus(outcomeStatus);
        log.setOutcomeCategory(outcomeCategory);
        log.setOutcomeReason(outcomeStatus.name());
        log.setAffectsFinalOutcome(affectsFinalOutcome);
        return log;
    }
}
