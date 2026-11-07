package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.FilterUpdateOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.AssignEmployeePage;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.OrganizationChangesJustificationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ActionElementsPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.openqa.selenium.WebDriver;

public class ManageOrganizationFactory {
    private final WebDriver driver;
    private DashboardManageOrganizationPage dashboardManageOrganizationPage;
    private FilterUpdateOrganizationPage filterUpdateOrganizationPage;
    private SetOrganizationPage setOrganizationPage;
    private ActionElementsPage actionElementsPage;
    private AssignEmployeePage assignEmployeePage;
    private OrganizationChangesJustificationPage organizationChangesJustificationPage;

    private SetOrganizationModalFactory setOrganizationModalFactory;
    private AssignEmployeeModalFactory assignEmployeeModalFactory;
    private CommonModalFactory commonModalFactory;
    private JustificationSectionFactory justificationSectionFactory;

    public ManageOrganizationFactory(WebDriver driver) {
        this.driver = driver;
    }

    public SetOrganizationModalFactory setOrganizationModals() {
        if (setOrganizationModalFactory == null) {
            setOrganizationModalFactory = new SetOrganizationModalFactory(driver);
        }
        return setOrganizationModalFactory;
    }

    public AssignEmployeeModalFactory assignEmployeeModals() {
        if (assignEmployeeModalFactory == null) {
            assignEmployeeModalFactory = new AssignEmployeeModalFactory(driver);
        }
        return assignEmployeeModalFactory;
    }

    public CommonModalFactory commonModals() {
        if (commonModalFactory == null) {
            commonModalFactory = new CommonModalFactory(driver);
        }
        return commonModalFactory;
    }

    public JustificationSectionFactory justificationSections() {
        if (justificationSectionFactory == null) {
            justificationSectionFactory = new JustificationSectionFactory(driver);
        }
        return justificationSectionFactory;
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

    public SetOrganizationPage getSetOrganizationFlow1Page() {
        if (setOrganizationPage == null) {
            setOrganizationPage = new SetOrganizationPage(driver);
        }
        return setOrganizationPage;
    }

    public AssignEmployeePage getAssignEmployeeFlow2Page() {
        if (assignEmployeePage == null) {
            assignEmployeePage = new AssignEmployeePage(driver);
        }
        return assignEmployeePage;
    }

    public ActionElementsPage getActionElementsPage() {
        if (actionElementsPage == null) {
            actionElementsPage = new ActionElementsPage(driver);
        }
        return actionElementsPage;
    }

    public OrganizationChangesJustificationPage getOrganizationChangesJustificationPage() {
        if (organizationChangesJustificationPage == null) {
            organizationChangesJustificationPage = new OrganizationChangesJustificationPage(driver);
        }
        return organizationChangesJustificationPage;
    }
}