package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.model.SlaSettingModel;
import org.example.utils.CustomAssert;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class SlaSettingTest extends SlaSettingBaseTest {

    @Test(groups = {"Sla Setting"}, description = "Check Detail Sla Setting Status Active", priority = 0)
    public void checkDetailSlaSettingActive(){
        logger.info("Starting test: checkDetailSlaSettingActive");

        logger.info("Step: Searching for an ACTIVE SLA Setting in table");
        WebElement slaActive = tableUtils().getSlaPeriodNameFromTable("ACTIVE");
        String slaName = slaActive.getText();
        logger.info("SLA Setting found: {}", slaName);

        logger.info("Step: Clicking Detail button for SLA Setting: {}", slaName);
        SlaSettingModel getDataSla = tableUtils().getSlaSettingFromTable(slaName);
        getDataSla.getDetailButton().click();

        logger.info("Step: Verifying SLA Setting Detail header");
        slaSettingDetail().isSlaSettingDetailHeader();

        logger.info("Step: Validating SLA Detail table content is not empty");
        CustomAssert.assertTrue(tableUtils().isTableContentValid(), "Table Sla Detail content is empty or invalid");

        logger.info("Finished test: checkDetailSlaSettingActive");
    }

    @Test(groups = {"Sla Setting"}, description = "Check Detail Sla Setting Status InActive")
    public void checkDetailSlaSettingInActive() {
        logger.info("Starting test: checkDetailSlaSettingInActive");

        logger.info("Step: Searching for an INACTIVE SLA Setting in table");
        WebElement slaInActive = tableUtils().getSlaPeriodNameFromTable("INACTIVE");
        String slaName = slaInActive.getText();
        logger.info("SLA Setting found: {}", slaName);

        logger.info("Step: Clicking Detail button for SLA Setting: {}", slaName);
        SlaSettingModel getDataSla = tableUtils().getSlaSettingFromTable(slaName);
        getDataSla.getDetailButton().click();

        logger.info("Step: Verifying SLA Setting Detail header");
        slaSettingDetail().isSlaSettingDetailHeader();

        logger.info("Step: Validating SLA Detail table content is not empty");
        CustomAssert.assertTrue(tableUtils().isTableContentValid(), "Table Sla Detail content is empty or invalid");

        logger.info("Finished test: checkDetailSlaSettingInActive");
    }

    @Test(groups = {"Delete"}, description = "Delete Sla Setting status Draft")
    public void deleteSlaSettingStatusDraft(){
        logger.info("Starting test: deleteSlaSettingStatusDraft");

        logger.info("Step: Searching for a DRAFT SLA Setting in table");
        WebElement slaDraft = tableUtils().getSlaPeriodNameFromTable("DRAFT");
        String slaName = slaDraft.getText();
        logger.info("SLA Setting found: {}", slaName);

        logger.info("Step: Clicking Delete button for SLA Setting: {}", slaName);
        SlaSettingModel getDataSla = tableUtils().getSlaSettingFromTable(slaName);
        getDataSla.getDeleteButton().click();
        
        actionDelete(slaName);

        logger.info("Finished test: deleteSlaSettingStatusDraft");
    }

    @Test(groups = {"Delete"}, description = "Delete Sla Setting status Not Started")
    public void deleteSlaSettingStatusNotStarted() {
        logger.info("Starting test: deleteSlaSettingStatusNotStarted");

        logger.info("Step: Searching for a NOT STARTED SLA Setting in table");
        WebElement slaNotStarted = tableUtils().getSlaPeriodNameFromTable("not started");
        String slaName = slaNotStarted.getText();
        logger.info("SLA Setting found: {}", slaName);

        logger.info("Step: Clicking Delete button for SLA Setting: {}", slaName);
        SlaSettingModel getDataSla = tableUtils().getSlaSettingFromTable(slaName);
        getDataSla.getDeleteButton().click();
        
        actionDelete(slaName);

        logger.info("Finished test: deleteSlaSettingStatusNotStarted");
    }

    private void actionDelete(String slaName){
        logger.info("Step: Confirming deletion in modal");
        deleteSlaSettingModal().isConfirmDelete();
        deleteSlaSettingModal().clickOkDeleteButton();
        
        logger.info("Step: Verifying success popup");
        deleteSlaSettingModal().isPopUpDeleteSuccess();

        logger.info("Step: Verifying SLA Setting '{}' is removed from dashboard", slaName);
        CustomAssert.assertTrue(
                dashboardSlaSettingPage().isSlaSettingDeleted(slaName),
                "SLA Setting '" + slaName + "' still exists in dashboard after deletion"
        );
    }
}
