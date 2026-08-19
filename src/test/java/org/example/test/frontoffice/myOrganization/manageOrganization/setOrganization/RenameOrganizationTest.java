package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgSheetName;
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

        data.get().setParentPosition("Head of Branch Transformation & Network Planning 1222");
        setOrganizationHelper().renameOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "Renamed Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()), "Primary Organization node is still displayed in the organization tree after rename");
        CustomAssert.assertTrue(setOrganizationHelper().verifyMissingParentPositionDisplayed(), "Missing parent position is not displayed after rename organization");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog("Rename Organization", data.get().getPrimaryOrganization(), data.get().getNewOrganizationName()), "Changes Log for Rename Organization is not displayed correctly");
    }



}
