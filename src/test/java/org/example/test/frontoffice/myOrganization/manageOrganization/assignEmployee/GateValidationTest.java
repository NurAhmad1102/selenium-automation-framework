package org.example.test.frontoffice.myOrganization.manageOrganization.assignEmployee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.enums.frontoffice.manageOrganization.ConfirmAction;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.annotations.Test;

public class GateValidationTest extends AssignEmployeeBaseTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Next button is Blocked when Unassigned Employee count > 0 (BR-18)")
    public void nextButtonBlockedWhenUnassignedExistsTest() {
        logger.info("Starting test: nextButtonBlockedWhenUnassignedExistsTest");

        assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.REMOVE, OrgMenuAction.REMOVE);

        String unassignedText = assignEmployeeHelper().assignEmployeePage().getUnassignedEmployeeCountText();
        logger.info("Current Unassigned Employee banner text: [{}]", unassignedText);

        CustomAssert.assertTrue(
                unassignedText.contains("Unassigned Employee"),
                "Banner Unassigned Employee harus tampil saat masih ada karyawan yang terdampak!"
        );

        logger.info("Test Completed Successfully: nextButtonBlockedWhenUnassignedExistsTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Next button is Allowed to proceed to Flow 3 when all Unassigned are assigned (BR-19)")
    public void nextButtonAllowedWhenUnassignedZeroTest() {
        logger.info("Starting test: nextButtonAllowedWhenUnassignedZeroTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.RENAME, OrgMenuAction.RENAME);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = assignEmployeeHelper().getFirstUnassignedEmployeeName();

        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        assignEmployeeHelper().assignEmployeePage().clickNextButton();

        logger.info("Test Completed Successfully: nextButtonAllowedWhenUnassignedZeroTest");
    }
}
