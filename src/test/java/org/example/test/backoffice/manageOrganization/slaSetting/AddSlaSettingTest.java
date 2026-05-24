package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.config.slaSettingStructure.MultipleConfig;
import org.example.config.slaSettingStructure.SingleConfig;
import org.example.model.SlaSettingModel;
import org.example.pageObject.backoffice.slaSettingMenu.checkboxAction.ActionType;
import org.example.utils.AssertionUtils;
import org.example.utils.CustomAssert;
import org.example.utils.TestDataFactory;
import org.example.utils.generateName.SlaPeriodNameService;
import org.testng.annotations.Test;

import java.util.List;

public class AddSlaSettingTest extends SlaSettingBaseTest {

    @Test(groups = {"Add"}, description = "Save Sla Setting as status ACTIVE, Rule: Effective Started = Today, Condition: Single")
    public void saveSlaSettingStatusActive(){
        logger.info("Starting test: saveSlaSettingStatusActive");

        String action = ActionType.getByIndex(2);
        List<String> actions = List.of(action);
        String periodName = SlaPeriodNameService.getGenerateSlaName();
        SingleConfig config = TestDataFactory.getSingleConfig();

        logger.info("Step: Creating SLA Setting with Period Name: {} and Single Configuration", periodName);
        String effectiveDate = slaSettingHelper().createSlaSetting(periodName, false, config, action);

        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);
        logger.info("Step: Validating SLA Detail table content");
        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail);
        CustomAssert.assertTrue(isDetailValid, "SLA Detail table content mismatch with expected configuration");

        logger.info("Step: Submitting SLA Setting");
        slaSettingHelper().submitSlaSetting();

        logger.info("Step: Verifying SLA Setting status is ACTIVE in dashboard");
        SlaSettingModel actualData = tableUtils().getSlaSettingFromTable(periodName);
        AssertionUtils.assertSlaSetting(actualData, effectiveDate, username(), "ACTIVE");
        CustomAssert.assertTrue(actualData.getDetailButton().isDisplayed(), "Detail button should be displayed for ACTIVE SLA Setting");

        logger.info("Finished test: saveSlaSettingStatusActive");
    }


    @Test(groups = {"Add"},priority = 1,description = "Save Sla Setting as status NOT STARTED, Rule: Future Effective Date \n Condition: Multiple")
    public void saveSlaSettingStatusNotStarted(){
        logger.info("Starting test: saveSlaSettingStatusNotStarted");

        List<String> actions = ActionType.getAll().subList(0, 3);
        String periodName = SlaPeriodNameService.getGenerateSlaName();
        MultipleConfig config = TestDataFactory.getMultipleConfig();

        logger.info("Step: Creating SLA Setting with Period Name: {} and Multiple Configuration", periodName);
        String effectiveDate = slaSettingHelper().createSlaSetting(periodName,true, config, actions);

        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);
        logger.info("Step: Validating SLA Detail table content");
        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail);
        CustomAssert.assertTrue(isDetailValid, "SLA Detail table content mismatch with expected multiple configuration");

        logger.info("Step: Submitting SLA Setting");
        slaSettingHelper().submitSlaSetting();

        logger.info("Step: Verifying SLA Setting status is NOT STARTED in dashboard");
        SlaSettingModel actualData = tableUtils().getSlaSettingFromTable(periodName);
        AssertionUtils.assertSlaSetting(actualData, effectiveDate, username(), "NOT STARTED");

        CustomAssert.assertTrue(actualData.getDeleteButton().isDisplayed(), "Delete button should be displayed for NOT STARTED SLA Setting");
        CustomAssert.assertTrue(actualData.getEditButton().isDisplayed(), "Edit button should be displayed for NOT STARTED SLA Setting");
        
        logger.info("Finished test: saveSlaSettingStatusNotStarted");
    }

    @Test(groups = {"Add"}, priority = 2 ,description = "Save Sla Setting as Draft, Status should be 'DRAFT'")
    public void saveSlaSettingAsDraft(){
        logger.info("Starting test: saveSlaSettingAsDraft");

        String periodName = SlaPeriodNameService.getGenerateSlaName();
        logger.info("Step: Adding SLA Setting as Draft with Period Name: {}", periodName);
        String effectiveDate = slaSettingHelper().addSlaSettingHelper(periodName, true);

        logger.info("Step: Clicking Save As Draft button");
        slaSettingPage().clickSaveAsDraftButton();
        slaDetailModal().clickCreateYesButton();

        logger.info("Step: Verifying success popup header");
        CustomAssert.assertTrue(slaDetailModal().isHeaderPopUpSuccess(), "Success popup header is not displayed after saving as draft");
        slaDetailModal().clickDonePopUpButton();

        logger.info("Step: Verifying SLA Setting status is DRAFT in dashboard");
        SlaSettingModel actualData = tableUtils().getSlaSettingFromTable(periodName);
        AssertionUtils.assertSlaSetting(actualData, effectiveDate, username(), "DRAFT");
        CustomAssert.assertTrue(actualData.getDeleteButton().isDisplayed(), "Delete button should be displayed for DRAFT SLA Setting");
        CustomAssert.assertTrue(actualData.getEditButton().isDisplayed(), "Edit button should be displayed for DRAFT SLA Setting");

        logger.info("Finished test: saveSlaSettingAsDraft");
    }

    @Test(groups = {"Add","Negative"}, description = "Create Duplicate, Sla Setting Already Exist")
    public void addDuplicatePeriodName(){
        logger.info("Starting test: addDuplicatePeriodName");

        List<String> actions = ActionType.getAll().subList(0, 3);
        String periodName = TestDataFactory.getPreviousSlaExisting().getSlaPeriod();
        MultipleConfig config = TestDataFactory.getMultipleConfig();

        logger.info("Step: Attempting to create duplicate SLA Setting with Period Name: {}", periodName);
        slaSettingHelper().createSlaSetting(periodName,true, config, actions);
        
        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);
        logger.info("Step: Validating SLA Detail table content before submission");
        boolean isSlaDetailEqual = tableUtils().validateRowSlaDetails(expectedDetail);
        CustomAssert.assertTrue(isSlaDetailEqual, "Data Sla Detail mismatch with expected configuration");
        
        logger.info("Step: Submitting and verifying duplicate alert");
        slaSettingPage().clickSubmitButton();
        slaDetailModal().clickCreateYesButton();
        CustomAssert.assertTrue(alertPopUpMessage().isAlertDuplicatePeriodeName(), "Duplicate Period Name alert message is not displayed");

        logger.info("Finished test: addDuplicatePeriodName");
    }

    @Test(groups = {"Add","Negative"},description = "Save Sla Setting without add Sla Details")
    public void saveEmptySlaSetting(){
        logger.info("Starting test: saveEmptySlaSetting");

        logger.info("Step: Adding SLA Setting without any details");
        slaSettingHelper().addSlaSettingHelper("Empty Sla Setting", true);
        
        logger.info("Step: Submitting and verifying empty details alert");
        slaSettingPage().clickSubmitButton();
        slaDetailModal().clickCreateYesButton();
        CustomAssert.assertTrue(alertPopUpMessage().isAlertSubmitSlaSettingEmpty(), "Empty SLA Setting alert message is not displayed");

        logger.info("Finished test: saveEmptySlaSetting");
    }

    @Test(groups = {"Add", "Negative"}, description = "Save Sla Setting without fill SLA Setting Period")
    public void saveEmptySLASettingPeriod(){
        logger.info("Starting test: saveEmptySLASettingPeriod");

        logger.info("Step: Adding SLA Setting with empty Period Name");
        slaSettingHelper().addSlaSettingHelper("", true);
        
        logger.info("Step: Submitting and verifying validation error");
        slaSettingPage().clickSubmitButton();
        slaSettingPage().isSlaSettingPeriodEmpty();

        logger.info("Finished test: saveEmptySLASettingPeriod");
    }

    @Test(groups = {"Add", "Negative"},description = "Save Sla Setting without fill Effective date")
    public void saveEmptyEffectiveDate(){
        logger.info("Starting test: saveEmptyEffectiveDate");

        logger.info("Step: Opening Add SLA Setting and filling Period Name");
        dashboardSlaSettingPage().clickAddSlaSettingButton();
        slaSettingPage().setPeriodNameField().sendKeys("Test Sla Period");
        
        logger.info("Step: Submitting without Effective Date and verifying validation");
        slaSettingPage().clickSubmitButton();
        slaSettingPage().isEffectiveDateEmpty();

        logger.info("Finished test: saveEmptyEffectiveDate");
    }

    @Test(groups = {"Add", "Negative"}, description = "Save Sla Setting without fill SLA Setting Period & Effective date")
    public void saveEmptySLASettingPeriodAndEffectiveDate(){
        logger.info("Starting test: saveEmptySLASettingPeriodAndEffectiveDate");

        logger.info("Step: Opening Add SLA Setting and submitting without required fields");
        dashboardSlaSettingPage().clickAddSlaSettingButton();
        slaSettingPage().clickSubmitButton();
        
        logger.info("Step: Verifying validation messages for both Period Name and Effective Date");
        slaSettingPage().isSlaSettingPeriodEmpty();
        slaSettingPage().isEffectiveDateEmpty();

        logger.info("Finished test: saveEmptySLASettingPeriodAndEffectiveDate");
    }

}
