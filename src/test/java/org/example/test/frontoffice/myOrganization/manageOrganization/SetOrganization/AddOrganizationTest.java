package org.example.test.frontoffice.myOrganization.manageOrganization.SetOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.model.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.utils.CustomAssert;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class AddOrganizationTest extends ManageOrganizationBaseTest {

    private OrganizationTestData data;

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from helper's default data
        this.data = setOrganizationHelper().getTestData();
    }

    @Test
    public void addNewOrganizationTest() {
        logger.info("Starting test: addNewOrganization");

        manageOrganizationHelper().goToFlow1();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        setOrganizationHelper().addNewOrganization(data);
        CustomAssert.assertTrue(setOrganizationModals().getAddJobPositionModal().isModalHeaderDisplayed(), "Add Job Position modal header is not displayed after adding new organization");
        setOrganizationHelper().fillJobPosition(data);

        setOrganizationHelper().confirmAction("Add");

        setOrganizationHelper().verifySuccessAndContinue("Added", "Selected organization has been successfully added");

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganization(data.getNewOrganizationName()), "New Organization node is not displayed in the organization tree");
    }

    @Test(description = "Verify that user cannot add new organization with existing organization name")
    public void addOrganizationWithExistingOrganizationNameTest() {
        logger.info("Starting test: addExistingOrganization");
        manageOrganizationHelper().goToFlow1();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        data.setNewOrganizationName(duplicateOrganizationName);
        setOrganizationHelper().addNewOrganization(data);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed");
    }

    @Test(description = "Verify that user cannot add new position with existing position name under the same or others organization")
    public void addOrganizationWithExistingPositionNameTest(){
        logger.info("Starting test: addExistingPositionName");
        manageOrganizationHelper().goToFlow1();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        data.setJobName(duplicatePositionName);
        setOrganizationHelper().addNewOrganization(data);
        CustomAssert.assertTrue(setOrganizationModals().getAddJobPositionModal().isModalHeaderDisplayed(), "Add Job Position modal header is not displayed after adding new organization");
        setOrganizationHelper().fillJobPosition(data);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicatePositionMessage), "Inline error message is not displayed");
    }
}