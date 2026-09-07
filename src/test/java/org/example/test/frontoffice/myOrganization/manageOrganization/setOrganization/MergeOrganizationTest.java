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

public class MergeOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.MERGE));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanMergeOrganizationContext() {
        data.remove();
    }

    @Test(groups = {"smoke"}, description = "Verify that user can Merge organization")
    public void mergeOrganizationTest(){
        logger.info("Starting test: Merge Organization");
        manageOrganizationHelper().goToFlow1SetOrganization();

        data.get().setParentPosition("Head of Branch Transformation & Network Planning 1222");
        setOrganizationHelper().mergeOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "Merged Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()), "Primary Organization node is still displayed in the organization tree after merge");
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationInvisible(data.get().getMergeOrganizationWith()), "Merge Organization With node is still displayed in the organization tree after merge");

        setOrganizationVerifier().verifyDetailNodeOrganization(data.get());
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationVerifier().verifyOrganizationChangesLog("Merge Organization", data.get().getPrimaryOrganization(), data.get().getMergeOrganizationWith(), data.get().getNewOrganizationName()), "Changes Log for Merge Organization is not displayed correctly");
    }

    @Test
    public void mergeOrganizationWithExistingPositionTest(){
        logger.info("Starting test: Merge Organization with existing position");
        manageOrganizationHelper().goToFlow1SetOrganization();
        data.get().setJobName(duplicatePositionName);

        setOrganizationHelper().mergeOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.MERGE);
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicatePositionMessage), "Inline error message is not displayed when merging organization with existing position name");
    }

    @Test
    public void mergeOrganizationWithExisingOrganizationNameTest(){
        logger.info("Starting test: Merge Organization with existing organization name");
        manageOrganizationHelper().goToFlow1SetOrganization();

        data.get().setNewOrganizationName(duplicateOrganizationName);
        setOrganizationHelper().mergeOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed when merging organization with existing organization name");
    }
}
