package org.example.test.frontoffice.myOrganization.manageOrganization.SetOrganization;

import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AddOrganizationTest extends ManageOrganizationBaseTest {
//    TODO: ganti data ini nanti
    int indexJobPositionSection = 0;
    int indexPositionSection = 0;
    String jobName = "Head of Advisory & Legal";
    String positionNamingType = "Same as Job";
    String location = "Kantor Pusat - Menara Asia";
    String parentPosition = "Network Sales Digitalization Head O223";
    String fteNumbers = "2";
    boolean isDottedMatrix = false;

    String parentOrganization = "O223 Network Sales Digitalization D2 ID";
    String newOrganizationName = "New Organization";
    String rcCode = "1611.00007.0401";

    @Test
    public void addNewOrganizationTest() throws InterruptedException {
        logger.info("Starting test: addNewOrganization");
        manageOrganizationHelper().goToFlow1();
        Assert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");
        setOrganizationHelper().addNewOrganization(parentOrganization, newOrganizationName, rcCode);
        Assert.assertTrue(addJobPositionModal().isModalHeaderDisplayed(), "Add Job Position Modal is not displayed after click next from new organization");
        setOrganizationHelper().fillJobPosition(
                indexJobPositionSection,
                indexPositionSection,
                jobName,
                positionNamingType,
                location,
                parentPosition,
                fteNumbers,
                isDottedMatrix
        );
        Thread.sleep(2000);
    }






















//    TODO:
    /*
  - BaseTest tetap validasi page loaded.
  - Helper tidak perlu punya method validasi page loaded.
  - ManageOrganizationFlow1 sebaiknya diganti menjadi test yang memvalidasi elemen/behavior spesifik di page, bukan hanya header yang sudah divalidasi setup.
*/
}
