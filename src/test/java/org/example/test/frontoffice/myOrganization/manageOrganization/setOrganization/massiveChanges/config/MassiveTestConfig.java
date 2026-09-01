package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.config;

public class MassiveTestConfig {

    // --- File Paths ---
    public static final String SOURCE_TEMPLATE_EXCEL_PATH = "src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/data/organization-development-flow1.xlsx";
    public static final String GENERATED_OUTPUT_EXCEL_PATH = "src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/generator/organization-development-flow1-generated.xlsx";

    // --- Generation & Execution Control Parameters ---
    public static final boolean AUTO_POPULATE_PROCEED = true; // true: unique seed suffix; false: static deterministic names
    public static final int TARGET_ORG_COUNT = 100; // Number of organizations to process (e.g. 5, 10, 15, 30, 50, 100; 0 for all)
    public static final int NEW_JOB_PERCENTAGE = 30; // Percentage of generated positions that use a dynamically created New Job (0 to 100%)

    // --- D0 Context Baseline Constants ---
    public static final String D0_ORGANIZATION = "Corporate Assurance ID";
    public static final String D0_PRIMARY_POSITION = "Chief Audit Executive";

    // --- Form Selection Constants ---
    public static final String POSITION_NAMING_TYPE = "Area of Responsibility / Job Scope";
}
