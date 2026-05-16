package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ManageOrganizationFlow1Test extends ManageOrganizationBaseTest {

    @Test(groups = {"Manage Organization"}, description = "Validate Manage Organization page is displayed")
    public void validateManageOrganizationPageIsDisplayed() {
        logger.info("Starting test: validateManageOrganizationPageIsDisplayed");

        Assert.assertTrue(
                manageOrganizationHelper().isManageOrganizationPageDisplayed(),
                "Expected Manage Organization page to be displayed, but it was not."
        );

        logger.info("Finished test: validateManageOrganizationPageIsDisplayed");
    }
//    TODO:
    /*
  - BaseTest tetap validasi page loaded.
  - Helper tidak perlu punya method validasi page loaded.
  - ManageOrganizationFlow1 sebaiknya diganti menjadi test yang memvalidasi elemen/behavior spesifik di page, bukan hanya header yang sudah divalidasi setup.
*/
}
