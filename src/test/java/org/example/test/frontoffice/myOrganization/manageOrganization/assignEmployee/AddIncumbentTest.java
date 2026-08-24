package org.example.test.frontoffice.myOrganization.manageOrganization.assignEmployee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.annotations.Test;

public class AddIncumbentTest extends AssignEmployeeBaseTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position using Unassigned Employee")
    public void addIncumbentFromUnassignedTest() {
        logger.info("Starting test: addIncumbentFromUnassignedTest");

        // 1. Data CURRENT & MOVEMENT: Membaca dari Sheet 'ASSIGN_EMPLOYEE' (Type: UNASSIGNED)
        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        // 2. Data PROPOSED & PRE-CONDITION FLOW 1: Membaca dari Sheet 'ADD_NEW'
        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip() != null && !currentAssignData.getNip().isEmpty()
                ? currentAssignData.getNip()
                : assignEmployeeHelper().getFirstUnassignedEmployeeName();

        // 3. Eksekusi Flow 2: Add Incumbent
        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        // 4. Validasi Changes Log
        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Add Incumbent", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Incumbent tidak sesuai!"
        );

        logger.info("Test Completed Successfully: addIncumbentFromUnassignedTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position using Existing Employee")
    public void addIncumbentFromExistingEmployeeTest() {
        logger.info("Starting test: addIncumbentFromExistingEmployeeTest");

        // 1. Data CURRENT & MOVEMENT: Membaca dari Sheet 'ASSIGN_EMPLOYEE' (Type: INSIDE)
        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        // 2. Data PROPOSED & PRE-CONDITION FLOW 1: Membaca dari Sheet 'ADD_NEW'
        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        // 3. Eksekusi Flow 2: Add Incumbent from Existing Employee
        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        // 4. Validasi Changes Log
        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Add Incumbent", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Existing Incumbent tidak sesuai!"
        );

        logger.info("Test Completed Successfully: addIncumbentFromExistingEmployeeTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent as PJS (Secondary Assignment)")
    public void addIncumbentAsPjsTest() {
        logger.info("Starting test: addIncumbentAsPjsTest");

        // 1. Data CURRENT & MOVEMENT: Membaca dari Sheet 'ASSIGN_EMPLOYEE' (Type: INSIDE)
        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        // 2. Data PROPOSED & PRE-CONDITION FLOW 1: Membaca dari Sheet 'ADD_NEW'
        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        // 3. Eksekusi Flow 2: Add Incumbent with Set as PJS = TRUE
        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                true // Set as PJS (Secondary Assignment)
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        // 4. Validasi Changes Log
        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Add Incumbent", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add PJS tidak sesuai!"
        );

        logger.info("Test Completed Successfully: addIncumbentAsPjsTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent button is Disabled when Position FTE is full (2/2)")
    public void addIncumbentWhenFteFullNegativeTest() {
        logger.info("Starting test: addIncumbentWhenFteFullNegativeTest");

        // 1. Data PRE-CONDITION FLOW 1: Membaca dari Sheet 'RENAME'
        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.RENAME, OrgMenuAction.RENAME);

        String targetOrg = proposedFlow1Data.getPrimaryOrganization();
        String targetPosition = proposedFlow1Data.getPositionName();

        // 2. Buka Manage Incumbent pada Org yang FTE-nya sudah penuh
        assignEmployeeHelper().assignEmployeePage().clickManageIncumbentOfNode(targetOrg);

        // 3. Validasi bahwa tombol 'Add Incumbent' berstatus DISABLED
        CustomAssert.assertTrue(
                assignEmployeeHelper().manageIncumbentModal().isAddIncumbentButtonDisabled(targetPosition),
                "Tombol Add Incumbent seharusnya Disabled saat kapasitas FTE sudah penuh!"
        );

        assignEmployeeHelper().manageIncumbentModal().clickCloseButton();
        logger.info("Test Completed Successfully: addIncumbentWhenFteFullNegativeTest");
    }
}
