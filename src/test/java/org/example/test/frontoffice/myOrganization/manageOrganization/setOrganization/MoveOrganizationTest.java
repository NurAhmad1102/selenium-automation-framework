package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.enums.frontoffice.manageOrganization.ConfirmAction;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.model.frontoffice.OrganizationTestData;
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

        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()),
                "Moved Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationDisplayed(data.get().getChildOrganization()),
                "Child Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()),
                "Primary Organization node is still displayed in the organization tree");

        setOrganizationVerifier().verifyDetailNodeOrganization(data.get());
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationVerifier().verifyOrganizationChangesLog("Move Organization", data.get().getPrimaryOrganization(), data.get().getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly");
    }

    @Test(groups = {"smoke"}, description = "Verify that user can't Move organization with existing organization name")
    public void moveOrganizationWithExistingOrganizationNameTest(){
        logger.info("Starting test: Move Organization");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setNewOrganizationName(duplicateOrganizationName);

        setOrganizationHelper().moveOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicateOrganizationMessage),
                "Inline error message is not displayed when moving organization with existing organization name");
    }

    @Test(groups = {"smoke"}, description = "Verify that user can't Move organization with existing position name")
    public void moveOrganizationDuplicatePositionNameTest() {
        logger.info("Starting test: Move Organization");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setJobName(duplicatePositionName);

        setOrganizationHelper().moveOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.MOVE);
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicatePositionMessage),
                "Inline error message is not displayed when moving organization with existing position name");

    }

}
