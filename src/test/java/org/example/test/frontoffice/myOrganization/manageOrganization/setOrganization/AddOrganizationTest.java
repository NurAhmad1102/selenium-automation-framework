package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.utils.CustomAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AddOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from Excel 'ADD_NEW' sheet
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.ADD_NEW));
    }

    @Test(groups = {"smoke"},description = "Verify that user can add new organization with valid data")
    public void addNewOrganizationTest() {
        logger.info("Starting test: addNewOrganization");
        manageOrganizationHelper().goToFlow1();

        setOrganizationHelper().addNewOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.ADD_NEW);

        setOrganizationHelper().confirmAction(ConfirmAction.ADD);
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "New Organization node is not displayed in the organization tree");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog("New Organization", data.get().getNewOrganizationName(), data.get().getPrimaryOrganization()), "Changes Log for New Organization is not displayed correctly");
    }

    @Test(description = "Verify that user cannot add new organization with existing organization name")
    public void addOrganizationWithExistingOrganizationNameTest() {
        logger.info("Starting test: addExistingOrganization");
        manageOrganizationHelper().goToFlow1();

        data.get().setNewOrganizationName(duplicateOrganizationName);
        setOrganizationHelper().addNewOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed");
    }

    @Test(description = "Verify that user cannot add new position with existing position name under the same or others organization")
    public void addOrganizationWithExistingPositionNameTest() {
        logger.info("Starting test: addExistingPositionName");
        manageOrganizationHelper().goToFlow1();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        data.get().setNewOrganizationName("NEW ORG POS TEST " + System.currentTimeMillis());
        data.get().setJobName(duplicatePositionName);
        setOrganizationHelper().addNewOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get(), OrgMenuAction.ADD_NEW);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicatePositionMessage), "Inline error message is not displayed");

    }
}