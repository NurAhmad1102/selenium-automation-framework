package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.utils.CustomAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RenameOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from Excel 'RENAME' sheet
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.RENAME));
    }

    @Test(groups = {"smoke"},description = "Verify that user can Rename organization")
    public void renameOrganizationTest(){
        logger.debug("Starting test: RenameOrganizationTest");
        manageOrganizationHelper().goToFlow1();

        setOrganizationHelper().renameOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "Renamed Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()), "Primary Organization node is still displayed in the organization tree after rename");
        CustomAssert.assertTrue(setOrganizationHelper().verifyMissingParentPositionDisplayed(), "Missing parent position is not displayed after rename organization");

        setOrganizationHelper().verifyDetailNodeOrganization(data.get());
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog("Rename Organization", data.get().getPrimaryOrganization(), data.get().getNewOrganizationName()), "Changes Log for Rename Organization is not displayed correctly");
    }

    @Test(groups = {"smoke"},description = "Verify that user can't Rename organization with existing organization name")
    public void renameOrganizationWithExistingOrganizationNameTest(){
        logger.debug("Starting test: RenameOrganizationTest");
        manageOrganizationHelper().goToFlow1();
        data.get().setNewOrganizationName(duplicateOrganizationName);

        setOrganizationHelper().renameOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed when renaming organization with existing organization name");
    }

    @Test(groups = {"smoke"},description = "Verify that user can't Rename organization with existing position name")
    public void renameOrganizationWithExistingPositionNameTest(){
        logger.debug("Starting test: RenameOrganizationTest");
        manageOrganizationHelper().goToFlow1();
        data.get().setJobName(duplicatePositionName);

        setOrganizationHelper().renameOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.RENAME);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicatePositionMessage), "Inline error message is not displayed when renaming organization with existing position name");
    }



}
