package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class MoveOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        // Initialize data from Excel 'MOVE' sheet
        this.data.set(setOrganizationHelper().getTestData("MOVE"));
    }

    @Test(groups = {"smoke"}, description = "Verify that user can Move organization")
    public void moveOrganizationTest(){
        logger.info("Starting test: Move Organization");
        manageOrganizationHelper().goToFlow1();

        data.get().setParentPosition("O223 Network Transformation D2 ID");
        setOrganizationHelper().moveOrganization(data.get());

    }

}
