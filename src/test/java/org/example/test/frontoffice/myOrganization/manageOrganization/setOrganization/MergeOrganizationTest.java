package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.utils.CustomAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MergeOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from Excel 'MERGE' sheet
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.MERGE));
    }

    @Test(groups = {"smoke"}, description = "Verify that user can Merge organization")
    public void mergeOrganizationTest(){
        logger.info("Starting test: Merge Organization");
        manageOrganizationHelper().goToFlow1SetOrganization();

        data.get().setParentPosition("Head of Branch Transformation & Network Planning 1222");
        setOrganizationHelper().mergeOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "Merged Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()), "Primary Organization node is still displayed in the organization tree after merge");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(data.get().getMergeOrganizationWith()), "Merge Organization With node is still displayed in the organization tree after merge");

        setOrganizationHelper().verifyDetailNodeOrganization(data.get());
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog("Merge Organization", data.get().getPrimaryOrganization(), data.get().getMergeOrganizationWith(), data.get().getNewOrganizationName()), "Changes Log for Merge Organization is not displayed correctly");
    }

    @Test
    public void mergeOrganizationWithExistingPositionTest(){
        logger.info("Starting test: Merge Organization with existing position");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setJobName(duplicatePositionName);

        setOrganizationHelper().mergeOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.MERGE);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicatePositionMessage), "Inline error message is not displayed when merging organization with existing position name");
    }

    @Test
    public void mergeOrganizationWithExisingOrganizationNameTest(){
        logger.info("Starting test: Merge Organization with existing organization name");
        manageOrganizationHelper().goToFlow1SetOrganization();

        data.get().setNewOrganizationName(duplicateOrganizationName);
        setOrganizationHelper().mergeOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed when merging organization with existing organization name");
    }
}
