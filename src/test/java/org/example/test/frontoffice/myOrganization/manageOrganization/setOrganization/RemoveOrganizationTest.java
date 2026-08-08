package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
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
        // Initialize data from Excel 'REMOVE' sheet
        this.data.set(setOrganizationHelper().getTestData("REMOVE"));
    }


    @Test( groups = {"smoke"},description = "Verify Remove existing organization")
    public void removeExistingOrganizationTest() {
        logger.info("Starting test: removeOrganization");
        String organizationToRemove = data.get().getPrimaryOrganization();        manageOrganizationHelper().goToFlow1();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        setOrganizationHelper().removeOrganization(organizationToRemove);
        setOrganizationHelper().confirmAction("Remove");

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(organizationToRemove), "Removed Organization node is still displayed in the organization tree");
    }

    @Test(description = "Verify Remove organization with tag new")
    public void removeNewOrganizationTest(){
        logger.info("Starting test: removeNewOrganization");
        
        OrganizationTestData addNewData = setOrganizationHelper().getTestData("ADD_NEW");
        String organizationToRemove = addNewData.getNewOrganizationName();

        manageOrganizationHelper().goToFlow1();

//        KODE PRE CONDITION ADD NEW ORG
        setOrganizationHelper().addNewOrganization(addNewData);
        setOrganizationHelper().fillJobPosition(addNewData, OrgMenuAction.ADD_NEW);
        setOrganizationHelper().confirmAction("Add");

//        KODE REMOVE ORG
        setOrganizationHelper().removeOrganization(organizationToRemove);
        setOrganizationHelper().confirmAction("Remove");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(organizationToRemove), "Removed Organization node is still displayed in the organization tree");;
    }

}
