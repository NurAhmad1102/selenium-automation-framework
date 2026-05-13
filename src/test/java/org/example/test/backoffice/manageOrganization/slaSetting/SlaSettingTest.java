package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.model.SlaSettingModel;
import org.example.pageObject.backoffice.slaSettingMenu.modal.deleteSlaSetting.DeleteSlaSettingModal;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SlaSettingTest extends SlaSettingBaseTest {

    @Test(groups = {"Sla Setting"}, description = "Check Detail Sla Setting Status Active", priority = 0)
    public void checkDetailSlaSettingActive(){
        logger.info("Start Test: Check Detail SLA Setting Status Active");

        WebElement slaActive = tableUtils().getSlaPeriodNameFromTable("ACTIVE");
        String slaName = slaActive.getText();
        logger.info("SLA Setting Status Active: {}", slaName);

        SlaSettingModel getDataSla = tableUtils().getSlaSettingFromTable(slaActive.getText());
        getDataSla.getDetailButton().click();

        slaSettingDetail().isSlaSettingDetailHeader();

        Assert.assertTrue(tableUtils().isTableContentValid(),
                "Table Sla Detail Empty");

        logger.info("End test: Check Detail SLA Setting Status Active");
    }

    @Test(groups = {"Sla Setting"}, description = "Check Detail Sla Setting Status InActive")
    public void checkDetailSlaSettingInActive() {
        logger.info("Start Test: Check Detail SLA Setting Status Inactive");

        WebElement slaInActive = tableUtils().getSlaPeriodNameFromTable("INACTIVE");
        String slaName = slaInActive.getText();
        logger.info("SLA Setting Status Inactive: {}", slaName);

        SlaSettingModel getDataSla = tableUtils().getSlaSettingFromTable(slaInActive.getText());
        getDataSla.getDetailButton().click();

        slaSettingDetail().isSlaSettingDetailHeader();

        Assert.assertTrue(tableUtils().isTableContentValid(),
                "Table Sla Detail Empty");

        logger.info("End Test: Check Detail SLA Setting Status Inactive");
    }

    @Test(groups = {"Delete"}, description = "Delete Sla Setting status Draft")
    public void deleteSlaSettingStatusDraft(){
        logger.info("Start Test: Delete SLA Setting Status Draft");

        WebElement slaDraft = tableUtils().getSlaPeriodNameFromTable("DRAFT");
        String slaName = slaDraft.getText();
        logger.info("SLA Setting Status Draft: {}", slaName);

        SlaSettingModel getDataSla = tableUtils().getSlaSettingFromTable(slaName);
        getDataSla.getDeleteButton().click();
        actionDelete(slaName);

        logger.info("End Test: Delete SLA Setting Status Draft");
    }

    @Test(groups = {"Delete"}, description = "Delete Sla Setting status Not Started")
    public void deleteSlaSettingStatusNotStarted() {
        logger.info("Start Test: Delete SLA Setting Status Not Started");

        WebElement slaNotStarted = tableUtils().getSlaPeriodNameFromTable("not started");
        String slaName = slaNotStarted.getText();
        logger.info("SLA Setting Status Not Started: {}", slaName);

        SlaSettingModel getDataSla = tableUtils().
                getSlaSettingFromTable(slaName);
        getDataSla.getDeleteButton().click();
        actionDelete(slaName);

        logger.info("End Test: Delete SLA Setting Status Not Started");
    }

    private void actionDelete(String slaName){
        deleteSlaSettingModal().isConfirmDelete();
        deleteSlaSettingModal().clickOkDeleteButton();
        deleteSlaSettingModal().isPopUpDeleteSuccess();

//        TODO: kasih wait
        Assert.assertTrue(
                dashboardSlaSettingPage().isSlaSettingDeleted(slaName),
                "Sla Setting still exist, failed to delete"
        );
    }
}
