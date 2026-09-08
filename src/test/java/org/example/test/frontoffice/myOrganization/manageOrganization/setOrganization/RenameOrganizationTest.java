package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.enums.frontoffice.manageOrganization.ConfirmAction;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.utils.CustomAssert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RenameOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.RENAME));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanRenameOrganizationContext() {
        data.remove();
    }

    @Test(groups = {"smoke"},description = "Verify that user can Rename organization")
    public void renameOrganizationTest(){
        logger.debug("Starting test: RenameOrganizationTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        setOrganizationHelper().renameOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "Renamed Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()), "Primary Organization node is still displayed in the organization tree after rename");
        CustomAssert.assertTrue(setOrganizationVerifier().verifyMissingParentPositionDisplayed(), "Missing parent position is not displayed after rename organization");

        setOrganizationVerifier().verifyDetailNodeOrganization(data.get());
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationVerifier().verifyOrganizationChangesLog("Rename Organization", data.get().getPrimaryOrganization(), data.get().getNewOrganizationName()), "Changes Log for Rename Organization is not displayed correctly");
    }

    @Test(groups = {"smoke"},description = "Verify that user can't Rename organization with existing organization name")
    public void renameOrganizationWithExistingOrganizationNameTest(){
        logger.debug("Starting test: RenameOrganizationTest");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setNewOrganizationName(duplicateOrganizationName);

        setOrganizationHelper().renameOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed when renaming organization with existing organization name");
    }

    @Test(groups = {"smoke"},description = "Verify that user can't Rename organization with existing position name")
    public void renameOrganizationWithExistingPositionNameTest(){
        logger.debug("Starting test: RenameOrganizationTest");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setJobName(duplicatePositionName);

        setOrganizationHelper().renameOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.RENAME);
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicatePositionMessage), "Inline error message is not displayed when renaming organization with existing position name");
    }



}
