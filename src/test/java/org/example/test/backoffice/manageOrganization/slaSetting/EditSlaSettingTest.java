package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.model.SlaSettingModel;
import org.example.pageObject.backoffice.slaSettingMenu.checkboxAction.ActionType;
import org.example.utils.CustomAssert;
import org.example.utils.generateName.SlaPeriodNameService;
import org.openqa.selenium.WebElement;
import org.testng.annotations.Test;

public class EditSlaSettingTest extends SlaSettingBaseTest {

    @Test(dependsOnGroups = "Add", groups = { "Edit" }, description = "Edit Sla Setting status Draft")
    public void editSlaSettingStatusDraft() {
        logger.info("Starting test: editSlaSettingStatusDraft");
        getSlaByStatus(true);
        logger.info("Finished test: editSlaSettingStatusDraft");
    }

    @Test(dependsOnGroups = "Add", groups = { "Edit" }, description = "Edit Sla Setting status Not Started")
    public void editSlaSettingStatusNotStarted() {
        logger.info("Starting test: editSlaSettingStatusNotStarted");
        getSlaByStatus(false);
        logger.info("Finished test: editSlaSettingStatusNotStarted");
    }

    private void getSlaByStatus(boolean isDraft) {
        String newSlaName = SlaPeriodNameService.getGenerateSlaName();
        String status = isDraft ? "draft" : "Not started";
        
        logger.info("Step: Searching for SLA Setting with status: {}", status);
        WebElement slaElement = tableUtils().getSlaPeriodNameFromTable(status);
        String oldSlaName = slaElement.getText();
        
        logger.info("Step: Editing SLA Setting: {}", oldSlaName);
        SlaSettingModel slaData = tableUtils().getSlaSettingFromTable(oldSlaName);
        slaData.getEditButton().click();

        logger.info("Step: Updating Period Name to: {}", newSlaName);
        WebElement inputPeriodName = slaSettingPage().setPeriodNameField();
        WebElement effDate = slaSettingPage().getEffectiveDateField();
        actionUtils().clearInputWithKeys(inputPeriodName);
        inputPeriodName.sendKeys(newSlaName);
        
        logger.info("Step: Updating Effective Date");
        effDate.click();
        slaSettingPage().pickEffectiveDate(true);

        validationEditResult(isDraft, oldSlaName, newSlaName);
    }

    private void validationEditResult(boolean isDraft, String oldSlaName, String newSlaName) {
        if (isDraft) {
            String action = ActionType.getByIndex(2);
            logger.info("Step: Adding SLA Detail for Draft status");
            slaSettingHelper().addSlaDetailHelper("Sla Type Edit", "20", action);
        }
        
        logger.info("Step: Submitting updated SLA Setting");
        slaSettingHelper().submitSlaSetting();
        
        logger.info("Step: Verifying edit results in dashboard");
        SlaSettingModel slaSettingModel = tableUtils().getSlaSettingFromTable(newSlaName);

        CustomAssert.assertTrue(dashboardSlaSettingPage().isSlaSettingDeleted(oldSlaName), "Old SLA Setting should no longer exist with previous name");
        CustomAssert.assertTrue(slaSettingModel.getEditButton().isDisplayed(), "Edit button should be displayed for the updated SLA Setting");
        CustomAssert.assertTrue(slaSettingModel.getDeleteButton().isDisplayed(), "Delete button should be displayed for the updated SLA Setting");
    }

}
