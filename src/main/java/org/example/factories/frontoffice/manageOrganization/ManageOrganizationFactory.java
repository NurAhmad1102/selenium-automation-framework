package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ActionElementsPage;
import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.FilterUpdateOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.openqa.selenium.WebDriver;

public class ManageOrganizationFactory {
    private final WebDriver driver;
    private DashboardManageOrganizationPage dashboardManageOrganizationPage;
    private FilterUpdateOrganizationPage filterUpdateOrganizationPage;
    private SetOrganizationPage setOrganizationPage;
    private ActionElementsPage actionElementsPage;
    private SetOrganizationModalFactory modalFactory;

    public ManageOrganizationFactory(WebDriver driver) {
        this.driver = driver;
    }

    public SetOrganizationModalFactory modals() {
        if (modalFactory == null) {
            modalFactory = new SetOrganizationModalFactory(driver);
        }
        return modalFactory;
    }

    public DashboardManageOrganizationPage getDashboardManageOrganizationPage() {
        if (dashboardManageOrganizationPage == null) {
            dashboardManageOrganizationPage = new DashboardManageOrganizationPage(driver);
        }
        return dashboardManageOrganizationPage;
    }

    public FilterUpdateOrganizationPage getFilterUpdateOrganizationPage() {
        if (filterUpdateOrganizationPage == null) {
            filterUpdateOrganizationPage = new FilterUpdateOrganizationPage(driver);
        }
        return filterUpdateOrganizationPage;
    }

    public SetOrganizationPage getSetOrganizationFlow1() {
        if (setOrganizationPage == null) {
            setOrganizationPage = new SetOrganizationPage(driver);
        }
        return setOrganizationPage;
    }

    public ActionElementsPage getActionElementsPage() {
        if (actionElementsPage == null) {
            actionElementsPage = new ActionElementsPage(driver);
        }
        return actionElementsPage;
    }

}
