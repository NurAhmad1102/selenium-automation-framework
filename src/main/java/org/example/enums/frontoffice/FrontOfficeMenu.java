package org.example.enums.frontoffice;

public enum FrontOfficeMenu {
    // Main Menus
    DASHBOARD("Dashboard"),
    BENEFITS("Benefits"),
    SELF_SERVICE("Self Service"),
    MY_ORGANIZATION("My Organization"),
    HYLEARN("HyLearn"),
    PERFORMANCE_MANAGEMENT("Performance Management"),
    HIRING_ONBOARDING("Hiring & Onboarding"),
    MPP_MONITORING("MPP Monitoring"),
    FEEDBACK_SURVEY("Feedback & Survey"),

    // Sub Menus (My Organization)
    ORGANIZATION_STRUCTURE("Organization Structure"),
    JOB_DESCRIPTION_COMPETENCIES("Job Description & Competencies"),
    EMPLOYEE_TRANSFER("Employee Transfer"),
    MANAGE_ORGANIZATION("Manage Organization"),
    ORGANIZATION_CHANGES_APPROVAL("Organization Changes Approval");

    private final String label;

    FrontOfficeMenu(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
