package org.example.test.frontoffice.myOrganization.manageOrganization.SetOrganization;

import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AddOrganizationTest extends ManageOrganizationBaseTest {

    @Test
    public void addNewOrganization() {
        logger.info("Starting test: addNewOrganization");
        manageOrganizationHelper().goToFlow1();
        Assert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");
        setOrganizationHelper().addNewOrganization("Budi ORG");

    }






















//    TODO:
    /*
  - BaseTest tetap validasi page loaded.
  - Helper tidak perlu punya method validasi page loaded.
  - ManageOrganizationFlow1 sebaiknya diganti menjadi test yang memvalidasi elemen/behavior spesifik di page, bukan hanya header yang sudah divalidasi setup.
*/
}
