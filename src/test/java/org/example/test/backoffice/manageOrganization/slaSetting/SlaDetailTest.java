package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.config.slaSettingStructure.SingleConfig;
import org.example.model.SlaDetailModel;
import org.example.pageObject.backoffice.slaSettingMenu.checkboxAction.ActionType;
import org.example.utils.CustomAssert;
import org.example.utils.TestDataFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class SlaDetailTest extends SlaSettingBaseTest {

    @Test (groups = {"Sla Detail"}, description = "Copy Sla Detail dari Previous Sla Setting 'SLA Period Q3 2025'")
    public void selectCopyFromPrevious(){
        logger.info("Starting test: selectCopyFromPrevious");

        logger.info("Step: Navigating to Add SLA Setting page");
        slaSettingHelper().addSlaSettingHelper("Try Copy SLA", true);
        
        logger.info("Step: Opening 'Copy from Previous' modal");
        slaSettingPage().clickCopyfromPreviousSettingButton();
        copyFromPreviousModal().isHeaderCopyFromPrevious();
        
        logger.info("Step: Selecting 'SLA Period Q3 2025' from dropdown");
        copyFromPreviousModal().clickFieldSelectSla();
        actionUtils().selectElementInDropdownByText("SLA Period Q3 2025");
        
        logger.info("Step: Clicking Add button in modal");
        copyFromPreviousModal().clickAddButton();

        logger.info("Step: Validating the number of rows in SLA Detail table");
        CustomAssert.assertEquals(tableUtils().getAllDataFromSlaDetailTable().size(), 3,
                "Number of rows in Sla Detail table mismatch after copying");

        logger.info("Finished test: selectCopyFromPrevious");
    }

    @Test (groups = {"Sla Detail"}, description = "Add Sla Detail")
    public void addSlaDetail(){
        logger.info("Starting test: addSlaDetail");

        String action = ActionType.getByIndex(2);
        List<String> actions = List.of(action);
        SingleConfig config = TestDataFactory.getSingleConfig();

        logger.info("Step: Creating SLA Setting and adding a Detail");
        slaSettingHelper().createSlaSetting("Try Add Sla Detail", false, config, action);
        
        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);
        logger.info("Step: Validating SLA Detail table content");
        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail);

        CustomAssert.assertTrue(isDetailValid, "SLA Detail table content mismatch with expected configuration");

        logger.info("Finished test: addSlaDetail");
    }


    @Test (groups = {"Sla Detail"}, description = "Delete Sla Detail dari Table Sla Detail")
    public void deleteSlaDetail(){
        logger.info("Starting test: deleteSlaDetail");
        
        logger.info("Step: Setting up pre-condition (Copying details)");
        preConditionSet();
        
        List<SlaDetailModel> slaDetail = tableUtils().getAllDataFromSlaDetailTable();
        int preDeleteCount = slaDetail.size();

        SlaDetailModel slaDetailModel = slaDetail.get(preDeleteCount - 2);
        String slaTypeDeleted = slaDetailModel.getSlaType();
        
        logger.info("Step: Selecting SLA Detail to delete: {}", slaTypeDeleted);
        slaDetailModel.getCheckbox().click();
        
        logger.info("Step: Clicking Delete SLA Detail button");
        slaSettingPage().clickDeleteSlaDetailButton();

        logger.info("Step: Verifying SLA Detail is deleted from table");
        int afterDeleteCount = tableUtils().getAllDataFromSlaDetailTable().size();
        CustomAssert.assertEquals(afterDeleteCount + 1, preDeleteCount, "Number of rows mismatch after deletion");
        CustomAssert.assertFalse(tableUtils().searchCellByText(slaTypeDeleted), "SLA Type '" + slaTypeDeleted + "' should be deleted from table");

        logger.info("Finished test: deleteSlaDetail");
    }

    @Test( groups = {"Sla Detail"},description = "Edit Sla Detail di Table Sla Detail")
    public void editSlaDetail(){
        logger.info("Starting test: editSlaDetail");
        
        logger.info("Step: Setting up pre-condition (Copying details)");
        preConditionSet();

        List<SlaDetailModel> slaDetail = tableUtils().getAllDataFromSlaDetailTable();
        int preEditCount = slaDetail.size();

        SlaDetailModel slaDetailModel = slaDetail.get(preEditCount - 2);
        String oldSlaType = slaDetailModel.getSlaType();
        
        logger.info("Step: Clicking Edit button for SLA Detail: {}", oldSlaType);
        slaDetailModel.getEditButton().click();

        String newSlaType = "Edit Test Type";
        String newSlaDay = "90";
        String condition = "MULTIPLE_CHANGES";
        List<String> actions = ActionType.getAll();

        logger.info("Step: Updating SLA Detail to Type: {}, Days: {}", newSlaType, newSlaDay);
        slaSettingHelper().editSlaDetailHelper(newSlaType, newSlaDay, actions);

        logger.info("Step: Verifying SLA Detail is updated in table");
        CustomAssert.assertFalse(tableUtils().searchCellByText(oldSlaType), "Old SLA Type '" + oldSlaType + "' should no longer exist");

        List<String> expectedDetail = new ArrayList<>(List.of(
                newSlaType,
                condition,
                newSlaDay
        ));
        expectedDetail.addAll(2, actions);

        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail);
        CustomAssert.assertTrue(isDetailValid, "SLA Detail table content mismatch after edit");

        logger.info("Finished test: editSlaDetail");
    }


    private void preConditionSet(){
        slaSettingHelper().addSlaSettingHelper("Try Delete SLA", true);
        slaSettingPage().clickCopyfromPreviousSettingButton();
        copyFromPreviousModal().clickFieldSelectSla();
        actionUtils().selectElementInDropdownByText("SLA Period Q3 2025");
        copyFromPreviousModal().clickAddButton();
    }

}
