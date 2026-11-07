package org.example.test.frontoffice.myOrganization.manageOrganization.assignEmployee;

import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.model.frontoffice.OrganizationTestData;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class AssignEmployeeBaseTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(ITestResult result) {
        super.initManageOrganizationContext(result);
        // Base context setup for Assign Employee Flow 2
    }

    @AfterMethod(alwaysRun = true)
    public void cleanAssignEmployeeContext() {
        data.remove();
    }

    protected OrganizationTestData getData() {
        return data.get();
    }

    protected void setData(OrganizationTestData testData) {
        this.data.set(testData);
    }
}
