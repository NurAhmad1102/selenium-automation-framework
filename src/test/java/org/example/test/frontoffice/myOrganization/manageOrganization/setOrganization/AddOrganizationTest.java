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

public class AddOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.ADD_NEW));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanAddOrganizationContext() {
        data.remove();
    }

    @Test(groups = {"smoke"},description = "Verify that user can add new organization with valid data")
    public void addNewOrganizationTest() {
        logger.info("Starting test: addNewOrganization");
        manageOrganizationHelper().goToFlow1SetOrganization();

        setOrganizationHelper().addNewOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.ADD_NEW);

        setOrganizationHelper().confirmAction(ConfirmAction.ADD);
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "New Organization node is not displayed in the organization tree");

        setOrganizationVerifier().verifyDetailNodeOrganization(data.get());

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationVerifier().verifyOrganizationChangesLog("New Organization", data.get().getNewOrganizationName(), data.get().getPrimaryOrganization()), "Changes Log for New Organization is not displayed correctly");
    }

    @Test(description = "Verify that user cannot add new organization with existing organization name")
    public void addOrganizationWithExistingOrganizationNameTest() {
        logger.info("Starting test: addExistingOrganization");
        manageOrganizationHelper().goToFlow1SetOrganization();

        data.get().setNewOrganizationName(duplicateOrganizationName);
        setOrganizationHelper().addNewOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed");
    }

    @Test(description = "Verify that user cannot add new position with existing position name under the same or others organization")
    public void addOrganizationWithExistingPositionNameTest() {
        logger.info("Starting test: addExistingPositionName");
        manageOrganizationHelper().goToFlow1SetOrganization();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        data.get().setNewOrganizationName("NEW ORG POS TEST " + System.currentTimeMillis());
        data.get().setJobName(duplicatePositionName);
        setOrganizationHelper().addNewOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.ADD_NEW);
        CustomAssert.assertTrue(setOrganizationVerifier().isInlineErrorMessageDisplayed(duplicatePositionMessage), "Inline error message is not displayed");

    }

    @Test(groups = {"smoke"},description = "Verify that user can add new organization with valid data")
    public void addOrganizationWithNewJobTest() {
        logger.info("Starting test: addNewOrganization");
        manageOrganizationHelper().goToFlow1SetOrganization();

        setOrganizationHelper().addNewOrganization(data.get());
        data.get().setJobName(createNewJobName);
        setOrganizationHelper().fillJobPositionWithNewJob(
                data.get(),
                OrgMenuAction.ADD_NEW,
                data.get().getJobName(),
                true);

        setOrganizationHelper().confirmAction(ConfirmAction.ADD);
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "New Organization node is not displayed in the organization tree");

        setOrganizationVerifier().verifyDetailNodeOrganization(data.get());

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationVerifier().verifyOrganizationChangesLog("New Organization", data.get().getNewOrganizationName(), data.get().getPrimaryOrganization()), "Changes Log for New Organization is not displayed correctly");
    }
}