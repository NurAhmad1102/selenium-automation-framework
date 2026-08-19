package org.example.test.frontoffice.myOrganization.manageOrganization.assignEmployee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.annotations.Test;

public class MoveIncumbentTest extends AssignEmployeeBaseTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Unassigned Employee to Renamed Organization using ASSIGN_EMPLOYEE sheet")
    public void moveUnassignedEmployeeToRenamedOrgTest() {
        logger.info("Starting test: moveUnassignedEmployeeToRenamedOrgTest");

        // 1. get dataa current Sheet 'ASSIGN_EMPLOYEE' (Type: UNASSIGNED)
        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        // 2. PRE-CONDITION FLOW 1: baca dari sheet 'RENAME'
        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.RENAME, OrgMenuAction.RENAME);

        // Target (Proposed) Data dari Excel Sheet RENAME
        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel() != null && !proposedFlow1Data.getLevel().isEmpty() ? proposedFlow1Data.getLevel() : "D2";
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        // curent employee dari excel Sheet ASSIGN_EMPLOYEE
        String employeeIdentifier = currentAssignData.getNip();

        // 3. eksekusi Flow 2 (Move Employee)
        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                currentAssignData.isPJS()
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        // 4. validasi Audit Changes Log
        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move Employee tidak tampil atau tidak sesuai!"
        );

        logger.info("Test Completed Successfully: moveUnassignedEmployeeToRenamedOrgTest");
    }
}
