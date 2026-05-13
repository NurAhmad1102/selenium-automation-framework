package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.config.slaSettingStructure.SingleConfig;
import org.example.model.SlaDetailModel;
import org.example.pageObject.backoffice.slaSettingMenu.checkboxAction.ActionType;
import org.example.utils.TestDataFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class SlaDetailTest extends SlaSettingBaseTest {

    @Test (groups = {"Sla Detail"}, description = "Copy Sla Detail dari Previous Sla Setting 'SLA Period Q3 2025'")
    public void selectCopyFromPrevious(){
        logger.info("Start Test: Select Copy From Previous");

        slaSettingHelper().addSlaSettingHelper("Try Copy SLA", true);
        slaSettingPage().clickCopyfromPreviousSettingButton();
        copyFromPreviousModal().isHeaderCopyFromPrevious();
        copyFromPreviousModal().clickFieldSelectSla();
        actionUtils().selectElementInDropdownByText("SLA Period Q3 2025");
        copyFromPreviousModal().clickAddButton();

        logger.info("Validation SLA Detail....");
        Assert.assertEquals(tableUtils().getAllDataFromSlaDetailTable().size(), 3,
                "Jumlah row di table Sla Detail tidak sama");

        logger.info("End Test: Select Copy From Previous");
    }

    @Test (groups = {"Sla Detail"}, description = "Add Sla Detail")
    public void addSlaDetail(){
        logger.info("Start Test: Add SLA Detail");

        String action = ActionType.getByIndex(2);
        List<String> actions = List.of(action);
        SingleConfig config = TestDataFactory.getSingleConfig();

        slaSettingHelper().createSlaSetting("Try Add Sla Detail", false, config, action);
        List<String> expectedDetail = slaSettingHelper().buildExpectedDetail(config, actions);
        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail); //sla detail table

        logger.info("Validation SLA Detail....");
        Assert.assertTrue(isDetailValid, "SLA Detail mismatch");

        logger.info("End Test: Add SLA Detail");
    }


    @Test (groups = {"Sla Detail"}, description = "Delete Sla Detail dari Table Sla Detail")
    public void deleteSlaDetail(){
        logger.info("Start Test: Delete SLA Detail");
        preConditionSet();
        List<SlaDetailModel> slaDetail = tableUtils().getAllDataFromSlaDetailTable();
        int preDeleteCount = slaDetail.size(); // count dari row

        SlaDetailModel slaDetailModel = slaDetail.get(preDeleteCount -2); //get index sebelum yg terahir
        String slaTypeDeleted = slaDetailModel.getSlaType();
        slaDetailModel.getCheckbox().click();
        slaSettingPage().clickDeleteSlaDetailButton();

        int afterDeleteCount = tableUtils().getAllDataFromSlaDetailTable().size(); // count dari row after delete

        logger.info("Validation SLA Detail....");
        Assert.assertEquals(afterDeleteCount+1, preDeleteCount, "Number of row mismatch");
        Assert.assertFalse(tableUtils().searchCellByText(slaTypeDeleted), "SLA Type should be deleted from table");

        logger.info("End Test: Delete SLA Detail");
    }

    @Test( groups = {"Sla Detail"},description = "Edit Sla Detail di Table Sla Detail")
    public void editSlaDetail(){
        logger.info("Start Test: Edit SLA Detail");
        preConditionSet();

        List<SlaDetailModel> slaDetail = tableUtils().getAllDataFromSlaDetailTable();
        int preEditCount = slaDetail.size();

        SlaDetailModel slaDetailModel = slaDetail.get(preEditCount-2);
        String slaType =  slaDetailModel.getSlaType();
        slaDetailModel.getEditButton().click();

        String newSlaType = "Edit Test Type";
        String newSlaDay = "90";
        String condition = "MULTIPLE_CHANGES";
        List<String> actions = ActionType.getAll();

        slaSettingHelper().editSlaDetailHelper(newSlaType,newSlaDay,actions);

        Assert.assertFalse(tableUtils().searchCellByText(slaType)); //kalo false, Type [only type validation] sudah berubah dan tidak ada di table sla detail

        List<String> expectedDetail = new ArrayList<>(List.of(
                newSlaType,
                condition,
                newSlaDay
        ));
        expectedDetail.addAll(2, actions);

        boolean isDetailValid = tableUtils().validateRowSlaDetails(expectedDetail); //sla detail table
        Assert.assertTrue(isDetailValid, "SLA Detail mismatch");

        logger.info("End Test: Edit SLA Detail");
    }


    private void preConditionSet(){
        slaSettingHelper().addSlaSettingHelper("Try Delete SLA", true);
        slaSettingPage().clickCopyfromPreviousSettingButton();
        copyFromPreviousModal().clickFieldSelectSla();
        actionUtils().selectElementInDropdownByText("SLA Period Q3 2025");
        copyFromPreviousModal().clickAddButton();
    }

    /*TODO:
     * 1. Add Sla Detail | done
     * 2. Edit Sla Detail | done
     * 3. Delete Sla Detail | done
     * 4. Copy from previous | done
     * */

}
