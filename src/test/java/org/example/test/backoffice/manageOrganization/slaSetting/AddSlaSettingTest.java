package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.config.slaSettingStructure.MultipleConfig;
import org.example.config.slaSettingStructure.SingleConfig;
import org.example.model.SlaSettingModel;
import org.example.pageObject.backoffice.slaSettingMenu.checkboxAction.ActionType;
import org.example.utils.AssertionUtils;
import org.example.utils.TestDataFactory;
import org.example.utils.generateName.SlaPeriodNameService;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class AddSlaSettingTest extends SlaSettingBaseTest {

    @Test(groups = {"Add"}, description = "Save Sla Setting as status ACTIVE, Rule: Effective Started = Today, Condition: Single")
    public void saveSlaSettingStatusActive(){
        logger.info("Start Test: Save SLA Setting Status Active");

        String action = ActionType.getByIndex(2);
        List<String> actions = List.of(action);
        String periodName = SlaPeriodNameService.getGenerateSlaName();
        SingleConfig config = TestDataFactory.getSingleConfig();

        String effectiveDate = slaSettingHelper().createSlaSetting(periodName, false, config, action);

        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);
        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail); //sla detail table

        logger.info("Validation SLA Detail....");
        Assert.assertTrue(isDetailValid, "SLA Detail mismatch");

        slaSettingHelper().submitSlaSetting();

        logger.info("Validation SLA Setting....");
        SlaSettingModel actualData = tableUtils().getSlaSettingFromTable(periodName);
        AssertionUtils.assertSlaSetting(actualData, effectiveDate, username(), "ACTIVE");
        Assert.assertTrue(actualData.getDetailButton().isDisplayed());

        logger.info("End Test: Save SLA Setting Status Active");
    }


    @Test(groups = {"Add"},priority = 1,description = "Save Sla Setting as status NOT STARTED, Rule: Future Effective Date \n Condition: Multiple")
    public void saveSlaSettingStatusNotStarted(){
        logger.info("Start Test: Save SLA Setting Status Not Started");

        List<String> actions = ActionType.getAll().subList(0, 3);
        String periodName = SlaPeriodNameService.getGenerateSlaName();
        MultipleConfig config = TestDataFactory.getMultipleConfig();

        String effectiveDate = slaSettingHelper().createSlaSetting(periodName,true, config, actions);

//        validasi sla detial yg baru di add
        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);

        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail); //sla detail table
        logger.info("Validation SLA Detail....");
        Assert.assertTrue(isDetailValid, "SLA Detail mismatch");

        slaSettingHelper().submitSlaSetting();
        logger.info("Validation SLA Setting....");
        SlaSettingModel actualData = tableUtils().getSlaSettingFromTable(periodName);
        AssertionUtils.assertSlaSetting(actualData, effectiveDate, username(), "NOT STARTED");

        Assert.assertTrue(actualData.getDeleteButton().isDisplayed());
        Assert.assertTrue(actualData.getEditButton().isDisplayed());
        logger.info("Start Test: Save SLA Setting Status Not Started");
    }

    @Test(groups = {"Add"}, priority = 2 ,description = "Save Sla Setting as Draft, Status should be 'DRAFT'")
    public void saveSlaSettingAsDraft(){
        logger.info("Start Test: Save SLA Setting Status Draft");

        String periodName = SlaPeriodNameService.getGenerateSlaName();
        String effectiveDate = slaSettingHelper().addSlaSettingHelper(periodName, true);

        slaSettingPage().clickSaveAsDraftButton();
        slaDetailModal().clickCreateYesButton();

        logger.info("Validation SLA Detail....");
        Assert.assertTrue(slaDetailModal().isHeaderPopUpSuccess());
        slaDetailModal().clickDonePopUpButton();

        SlaSettingModel actualData = tableUtils().getSlaSettingFromTable(periodName);

        logger.info("Validation SLA Setting....");
        AssertionUtils.assertSlaSetting(actualData, effectiveDate, username(), "DRAFT");
        Assert.assertTrue(actualData.getDeleteButton().isDisplayed());
        Assert.assertTrue(actualData.getEditButton().isDisplayed());

        logger.info("Start Test: Save SLA Setting Status Draft");
    }

    @Test(groups = {"Add","Negative"}, description = "Create Duplicate, Sla Setting Already Exist")
    public void addDuplicatePeriodName(){
        logger.info("Start Test: Create Duplicate, SLA Setting Already Exist");

        List<String> actions = ActionType.getAll().subList(0, 3);
        String periodName = TestDataFactory.getPreviousSlaExisting().getSlaPeriod();
        MultipleConfig config = TestDataFactory.getMultipleConfig();

        slaSettingHelper().createSlaSetting(periodName,true, config, actions);
        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);

        boolean isSlaDetailEqual = tableUtils().validateRowSlaDetails(expectedDetail);
        Assert.assertTrue(isSlaDetailEqual, "Data Sla Detail tidak sama dengan Data yang muncul di Table Sla Detail");
        slaSettingPage().clickSubmitButton();
        slaDetailModal().clickCreateYesButton();
        Assert.assertTrue(alertPopUpMessage().isAlertDuplicatePeriodeName());

        logger.info("End Test: Create Duplicate, SLA Setting Already Exist");
    }

    @Test(groups = {"Add","Negative"},description = "Save Sla Setting without add Sla Details")
    public void saveEmptySlaSetting(){
        logger.info("Start Test: Save SLA Setting Status Without Add SLA Details");

        slaSettingHelper().addSlaSettingHelper("Empty Sla Setting", true);
        slaSettingPage().clickSubmitButton();
        slaDetailModal().clickCreateYesButton();
        Assert.assertTrue(alertPopUpMessage().isAlertSubmitSlaSettingEmpty());

        logger.info("End Test: Save SLA Setting Status Without Add SLA Details");
    }

    @Test(groups = {"Add", "Negative"}, description = "Save Sla Setting without fill SLA Setting Period")
    public void saveEmptySLASettingPeriod(){
        logger.info("Start Test: Save SLA Setting Status Without Fill SLA Setting Period");

        slaSettingHelper().addSlaSettingHelper("", true);
        slaSettingPage().clickSubmitButton();
        slaSettingPage().isSlaSettingPeriodEmpty();

        logger.info("End Test: Save SLA Setting Status Without Fill SLA Setting Period");
    }

    @Test(groups = {"Add", "Negative"},description = "Save Sla Setting without fill Effective date")
    public void saveEmptyEffectiveDate(){
        logger.info("Start Test: Save SLA Setting Status Without Fill Effective date");

        dashboardSlaSettingPage().clickAddSlaSettingButton();
        slaSettingPage().setPeriodNameField().sendKeys("Test Sla Period");
        slaSettingPage().clickSubmitButton();
        slaSettingPage().isEffectiveDateEmpty();

        logger.info("End Test: Save SLA Setting Status Without Fill Effective date");
    }

    @Test(groups = {"Add", "Negative"}, description = "Save Sla Setting without fill SLA Setting Period & Effective date")
    public void saveEmptySLASettingPeriodAndEffectiveDate(){
        logger.info("Start Test: Save SLA Setting Status Without Fill SLA Setting Period & Effective date");

        dashboardSlaSettingPage().clickAddSlaSettingButton();
        slaSettingPage().clickSubmitButton();
        slaSettingPage().isSlaSettingPeriodEmpty();
        slaSettingPage().isEffectiveDateEmpty();

        logger.info("End Test: Save SLA Setting Status Without Fill SLA Setting Period & Effective date");
    }

}



