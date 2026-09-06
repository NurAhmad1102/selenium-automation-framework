package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ActionElementsPage;
import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.FilterUpdateOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.AssignEmployeePage;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.CurrentOrganizationSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.NewJobSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.NewPositionSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.OrganizationChangesJustificationPage;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.ProposedAssignmentEmployeeSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.ProposedOrganizationSection;
import org.openqa.selenium.WebDriver;

public class ManageOrganizationFactory {
    private final WebDriver driver;
    private DashboardManageOrganizationPage dashboardManageOrganizationPage;
    private FilterUpdateOrganizationPage filterUpdateOrganizationPage;
    private SetOrganizationPage setOrganizationPage;
    private ActionElementsPage actionElementsPage;
    private SetOrganizationModalFactory modalFactory;
    private AssignEmployeePage assignEmployeePage;
    private AssignEmployeeModalFactory assignEmployeeModalFactory;

    private OrganizationChangesJustificationPage organizationChangesJustificationPage;
    private CurrentOrganizationSection currentOrganizationSection;
    private ProposedOrganizationSection proposedOrganizationSection;
    private ProposedAssignmentEmployeeSection proposedAssignmentEmployeeSection;
    private NewPositionSection newPositionSection;
    private NewJobSection newJobSection;

    public ManageOrganizationFactory(WebDriver driver) {
        this.driver = driver;
    }

    public SetOrganizationModalFactory modals() {
        if (modalFactory == null) {
            modalFactory = new SetOrganizationModalFactory(driver);
        }
        return modalFactory;
    }

    public AssignEmployeeModalFactory assignEmployeeModals() {
        if (assignEmployeeModalFactory == null) {
            assignEmployeeModalFactory = new AssignEmployeeModalFactory(driver);
        }
        return assignEmployeeModalFactory;
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

    public CurrentOrganizationSection getCurrentOrganizationSection() {
        if (currentOrganizationSection == null) {
            currentOrganizationSection = new CurrentOrganizationSection(driver);
        }
        return currentOrganizationSection;
    }

    public ProposedOrganizationSection getProposedOrganizationSection() {
        if (proposedOrganizationSection == null) {
            proposedOrganizationSection = new ProposedOrganizationSection(driver);
        }
        return proposedOrganizationSection;
    }

    public ProposedAssignmentEmployeeSection getProposedAssignmentEmployee() {
        if (proposedAssignmentEmployeeSection == null) {
            proposedAssignmentEmployeeSection = new ProposedAssignmentEmployeeSection(driver);
        }
        return proposedAssignmentEmployeeSection;
    }

    public NewPositionSection getNewPositionSection() {
        if (newPositionSection == null) {
            newPositionSection = new NewPositionSection(driver);
        }
        return newPositionSection;
    }

    public NewJobSection getNewJobSection() {
        if (newJobSection == null) {
            newJobSection = new NewJobSection(driver);
        }
        return newJobSection;
    }
}
