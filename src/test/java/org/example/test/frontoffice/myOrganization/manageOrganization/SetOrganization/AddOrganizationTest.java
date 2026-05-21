package org.example.test.frontoffice.myOrganization.manageOrganization.SetOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.model.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
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
        Assert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        setOrganizationHelper().addNewOrganization(data);
        Assert.assertTrue(setOrganizationModals().getAddJobPositionModal().isModalHeaderDisplayed(), "Add Job Position modal header is not displayed after adding new organization");
        setOrganizationHelper().fillJobPosition(data);

        setOrganizationHelper().confirmAction("Add");

        setOrganizationHelper().verifySuccessAndContinue("Added", "Selected organization has been successfully added");

        Assert.assertTrue(setOrganizationHelper().isNodeOrganization(data.getNewOrganizationName()));
    }

}






















//    TODO:
    /*
  - BaseTest tetap validasi page loaded.
  - Helper tidak perlu punya method validasi page loaded.
  - ManageOrganizationFlow1 sebaiknya diganti menjadi test yang memvalidasi elemen/behavior spesifik di page, bukan hanya header yang sudah divalidasi setup.
*/

