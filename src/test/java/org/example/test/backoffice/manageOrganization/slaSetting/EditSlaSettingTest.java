package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.model.SlaSettingModel;
import org.example.pageObject.backoffice.slaSettingMenu.checkboxAction.ActionType;
import org.example.utils.generateName.SlaPeriodNameService;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class EditSlaSettingTest extends SlaSettingBaseTest {
    // private String slaNameEdit = "Summary Quarter Update";

    @Test(dependsOnGroups = "Add", groups = { "Edit" }, description = "Edit Sla Setting status Draft")
    public void editSlaSettingStatusDraft() {
        logger.info("Start Test: Edit SLA Setting Status Draft");
        getSlaByStatus(true);

        logger.info("End Test: Edit SLA Setting Status Draft");
    }

    @Test(dependsOnGroups = "Add", groups = { "Edit" }, description = "Edit Sla Setting status Not Started")
    public void editSlaSettingStatusNotStarted() {
        logger.info("Start Test: Edit SLA Setting Status Not Started");
        getSlaByStatus(false);

        logger.info("End Test: Edit SLA Setting Status Not Started");
    }

    private void getSlaByStatus(boolean isDraft) {
        String newSlaName = SlaPeriodNameService.getGenerateSlaName();

        String status = "Not started";
        if (isDraft) {
            status = "draft";
        }
        String slaName = tableUtils().getSlaPeriodNameFromTable(status).getText();
        SlaSettingModel slaData = tableUtils().getSlaSettingFromTable(slaName);
        slaData.getEditButton().click();

        WebElement inputPeriodName = slaSettingPage().setPeriodNameField();
        WebElement effDate = slaSettingPage().getEffectiveDateField();
        actionUtils().clearInputWithKeys(inputPeriodName);
        inputPeriodName.sendKeys(newSlaName);
        effDate.click();
        actionUtils().setPickDateCalender(true);

        validationEditResult(isDraft, slaName, newSlaName);
    }

    private void validationEditResult(boolean isDraft, String oldSlaName, String newSlaName) {
        if (isDraft) {
            String action = ActionType.getByIndex(2);
            slaSettingHelper().addSlaDetailHelper("Sla Type Edit", "20", action); // TODO: here changed
        }
        slaSettingHelper().submitSlaSetting();
        SlaSettingModel slaSettingModel = tableUtils().getSlaSettingFromTable(newSlaName);

        logger.info("Validation SLA Setting....");
        Assert.assertTrue(dashboardSlaSettingPage().isSlaSettingDeleted(oldSlaName));
        Assert.assertTrue(slaSettingModel.getEditButton().isDisplayed());
        Assert.assertTrue(slaSettingModel.getDeleteButton().isDisplayed());
    }

}
