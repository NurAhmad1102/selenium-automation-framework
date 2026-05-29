package org.example.test.frontoffice.myOrganization.manageOrganization.SetOrganization;

import org.example.base.BaseTest;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.model.OrganizationTestData;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.utils.CustomAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MergeOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from helper's default data
        this.data.set(setOrganizationHelper().getTestData());
    }

    @Test
    public void mergeOrganizationTest(){
        logger.info("Starting test: Merge Organization");
        manageOrganizationHelper().goToFlow1();

        data.get().setParentPosition("Head of Branch Transformation & Network Planning 1222");
        setOrganizationHelper().mergeOrganization(data.get());
        setOrganizationHelper().fillJobPosition(data.get());
        setOrganizationHelper().confirmAction("Merge");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(data.get().getNewOrganizationName()), "Merged Organization node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(data.get().getPrimaryOrganization()), "Primary Organization node is still displayed in the organization tree after merge");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(data.get().getMergeOrganizationWith()), "Merge Organization With node is still displayed in the organization tree after merge");
    }
}
