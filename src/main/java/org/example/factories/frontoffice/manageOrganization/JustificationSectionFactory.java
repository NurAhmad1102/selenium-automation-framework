package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.CurrentOrganizationSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.NewJobSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.NewPositionSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.ProposedAssignmentEmployeeSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.ProposedOrganizationSection;
import org.openqa.selenium.WebDriver;

public class JustificationSectionFactory {
    private final WebDriver driver;
    private CurrentOrganizationSection currentOrganizationSection;
    private ProposedOrganizationSection proposedOrganizationSection;
    private ProposedAssignmentEmployeeSection proposedAssignmentEmployeeSection;
    private NewPositionSection newPositionSection;
    private NewJobSection newJobSection;

    public JustificationSectionFactory(WebDriver driver) {
        this.driver = driver;
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