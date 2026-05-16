package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.openqa.selenium.WebDriver;

public class ManageOrganizationFactory {
    private final WebDriver driver;
    private DashboardManageOrganizationPage dashboardManageOrganizationPage;

    public ManageOrganizationFactory(WebDriver driver) {
        this.driver = driver;
    }

    public DashboardManageOrganizationPage getDashboardManageOrganizationPage() {
        if (dashboardManageOrganizationPage == null) {
            dashboardManageOrganizationPage = new DashboardManageOrganizationPage(driver);
        }
        return dashboardManageOrganizationPage;
    }
}
