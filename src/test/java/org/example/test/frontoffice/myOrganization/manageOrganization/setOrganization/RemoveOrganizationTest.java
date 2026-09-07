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

public class RemoveOrganizationTest extends ManageOrganizationBaseTest {
    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.REMOVE));
    }

    @AfterMethod(alwaysRun = true)
    public void cleanRemoveOrganizationContext() {
        data.remove();
    }


    @Test( groups = {"smoke"},description = "Verify Remove existing organization")
    public void removeExistingOrganizationTest() {
        logger.info("Starting test: removeOrganization");
        String organizationToRemove = data.get().getPrimaryOrganization();        manageOrganizationHelper().goToFlow1SetOrganization();
        CustomAssert.assertTrue(setOrganizationFlow1Page().isSetOrganizationHeaderDisplayed(), "Set Organization header is not displayed");

        setOrganizationHelper().removeOrganization(organizationToRemove);
        setOrganizationHelper().confirmAction(ConfirmAction.REMOVE);

        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationInvisible(organizationToRemove),
                "Removed Organization node is still displayed in the organization tree");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationVerifier().verifyOrganizationChangesLog("Remove Organization", organizationToRemove),
                "Changes Log for Remove Organization is not displayed correctly");
    }

    @Test(description = "Verify Remove organization with tag new")
    public void removeNewOrganizationTest(){
        logger.info("Starting test: removeNewOrganization");
        
        OrganizationTestData addNewData = setOrganizationHelper().getTestData(OrgSheetName.ADD_NEW);
        String organizationToRemove = addNewData.getNewOrganizationName();

        manageOrganizationHelper().goToFlow1SetOrganization();

//        pre-condition
        setOrganizationHelper().addNewOrganization(addNewData);
        setOrganizationHelper().fillJobPosition(addNewData, OrgMenuAction.ADD_NEW);
        setOrganizationHelper().confirmAction(ConfirmAction.ADD);

//        remove org
        setOrganizationHelper().removeOrganization(organizationToRemove);
        setOrganizationHelper().confirmAction(ConfirmAction.REMOVE);
        CustomAssert.assertTrue(setOrganizationVerifier().isNodeOrganizationInvisible(organizationToRemove),
                "Removed Organization node is still displayed in the organization tree");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationVerifier().verifyOrganizationChangesLog("Remove Organization", organizationToRemove),
                "Changes Log for Remove Organization is not displayed correctly");
    }

}
