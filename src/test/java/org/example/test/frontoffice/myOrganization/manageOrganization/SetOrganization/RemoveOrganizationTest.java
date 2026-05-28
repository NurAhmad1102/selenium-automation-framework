package org.example.test.frontoffice.myOrganization.manageOrganization.SetOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.model.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.utils.CustomAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RemoveOrganizationTest extends ManageOrganizationBaseTest {
    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from helper's default data
        this.data.set(setOrganizationHelper().getTestData());
    }


    @Test(description = "Verify Remove existing organization")
    public void removeExistingOrganizationTest() {
        logger.info("Starting test: removeOrganization");
        String organizationToRemove = data.get().getPrimaryOrganization();        manageOrganizationHelper().goToFlow1();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        setOrganizationHelper().removeOrganization(organizationToRemove);
        setOrganizationHelper().confirmAction("Remove");
        setOrganizationHelper().verifySuccessAndContinue("Removed");

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(organizationToRemove), "Removed Organization node is still displayed in the organization tree");
    }

    @Test(description = "Verify Remove organization with tag new")
    public void removeNewOrganizationTest(){
        logger.info("Starting test: removeNewOrganization");
        String organizationToRemove = data.get().getNewOrganizationName();

        manageOrganizationHelper().goToFlow1();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

//        KODE PRE CONDITION ADD NEW ORG
        setOrganizationHelper().addNewOrganization(data.get());
        CustomAssert.assertTrue(setOrganizationModals().getAddJobPositionModal().isModalHeaderDisplayed(), "Add Job Position modal header is not displayed after adding new organization");
        setOrganizationHelper().fillJobPosition(data.get());
        setOrganizationHelper().confirmAction("Add");
        setOrganizationHelper().verifySuccessAndContinue("Added");

//        KODE REMOVE ORG
        setOrganizationHelper().removeOrganization(organizationToRemove);
        setOrganizationHelper().confirmAction("Remove");
        setOrganizationHelper().verifySuccessAndContinue("Removed");
        CustomAssert.assertFalse(setOrganizationHelper().isNodeOrganizationDisplayed(organizationToRemove), "Removed Organization node is still displayed in the organization tree");
    }

}
