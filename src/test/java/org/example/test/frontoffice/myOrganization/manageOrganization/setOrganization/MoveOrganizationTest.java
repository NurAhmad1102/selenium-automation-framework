package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MoveOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from Excel 'MOVE' sheet
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.MOVE));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanMoveOrganizationContext() {
        data.remove();
    }

    @Test(groups = {"smoke"}, description = "Verify that user can Move organization")
    public void moveOrganizationTest(){
        logger.info("Starting test: Move Organization");
        manageOrganizationHelper().goToFlow1SetOrganization();

        setOrganizationHelper().moveOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()),
                "Moved Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(data.get().getChildOrganization()),
                "Child Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()),
                "Primary Organization node is still displayed in the organization tree");

        setOrganizationHelper().verifyDetailNodeOrganization(data.get());
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog("Move Organization", data.get().getPrimaryOrganization(), data.get().getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly");
    }

    @Test(groups = {"smoke"}, description = "Verify that user can't Move organization with existing organization name")
    public void moveOrganizationWithExistingOrganizationNameTest(){
        logger.info("Starting test: Move Organization");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setNewOrganizationName(duplicateOrganizationName);

        setOrganizationHelper().moveOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage),
                "Inline error message is not displayed when moving organization with existing organization name");
    }

    @Test(groups = {"smoke"}, description = "Verify that user can't Move organization with existing position name")
    public void moveOrganizationDuplicatePositionNameTest() {
        logger.info("Starting test: Move Organization");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setJobName(duplicatePositionName);

        setOrganizationHelper().moveOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.MOVE);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicatePositionMessage),
                "Inline error message is not displayed when moving organization with existing position name");

    }

}
