package org.example.test.frontoffice.myOrganization.manageOrganization.assignEmployee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.example.utils.CustomSoftAssert;
import org.testng.annotations.Test;

public class MoveIncumbentTest extends AssignEmployeeBaseTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Unassigned Employee to Renamed Organization using ASSIGN_EMPLOYEE sheet")
    public void moveUnassignedEmployeeToRenamedOrgTest() {
        logger.info("Starting test: moveUnassignedEmployeeToRenamedOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.RENAME, OrgMenuAction.RENAME);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position"
        );

        CustomSoftAssert softAssert = new CustomSoftAssert();

        //used this if only there is one unassigned employee as pre-conditon
        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "View Unassigned button is still clickable, indicating the employee is still unassigned"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move Employee tidak sesuai!"
        );
        softAssert.assertAll();

        logger.info("Test Completed Successfully: moveUnassignedEmployeeToRenamedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Unassigned Employee to Added Organization using ASSIGN_EMPLOYEE sheet")
    public void moveUnassignedEmployeeToAddedNewOrgTest() {
        logger.info("Starting test: moveUnassignedEmployeeToAddNewOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        // data awal sebelum diremove
        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().removeIncumbent(sourceOrg, sourcePosition, employeeIdentifier);
        assignEmployeeHelper().confirmAction(ConfirmAction.REMOVE);

        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position"
        );

        CustomSoftAssert softAssert = new CustomSoftAssert();

        //used this if only there is one unassigned employee as pre-conditon
        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "View Unassigned button is still clickable, indicating the employee is still unassigned"
        );
        assignEmployeeHelper().closeManageIncumbentModal();
        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move Employee tidak sesuai!"
        );
        softAssert.assertAll();

        logger.info("Test Completed Successfully: moveUnassignedEmployeeToRenamedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Unassigned Employee to Moved Organization using ASSIGN_EMPLOYEE sheet")
    public void moveUnassignedEmployeeToMovedOrgTest() {
        logger.info("Starting test: moveUnassignedEmployeeToMovedOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.MOVE, OrgMenuAction.MOVE);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position"
        );

        CustomSoftAssert softAssert = new CustomSoftAssert();

        //used this if only there is one unassigned employee as pre-conditon
        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "View Unassigned button is still clickable, indicating the employee is still unassigned"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move Employee tidak sesuai!"
        );
        softAssert.assertAll();

        logger.info("Test Completed Successfully: moveUnassignedEmployeeToRenamedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Unassigned Employee to Split Organization using ASSIGN_EMPLOYEE sheet")
    public void moveUnassignedEmployeeToSplitOrgTest() {
        logger.info("Starting test: moveUnassignedEmployeeToSplitOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.SPLIT, OrgMenuAction.SPLIT);

        String targetOrg = proposedFlow1Data.getNewOrganizationName1();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position"
        );

        CustomSoftAssert softAssert = new CustomSoftAssert();

        //used this if only there is one unassigned employee as pre-conditon
        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "View Unassigned button is still clickable, indicating the employee is still unassigned"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move Employee tidak sesuai!"
        );
        softAssert.assertAll();

        logger.info("Test Completed Successfully: moveUnassignedEmployeeToRenamedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Unassigned Employee to Merged Organization using ASSIGN_EMPLOYEE sheet")
    public void moveUnassignedEmployeeToMergedOrgTest() {
        logger.info("Starting test: moveUnassignedEmployeeToMergedOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.MERGE, OrgMenuAction.MERGE);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position"
        );

        CustomSoftAssert softAssert = new CustomSoftAssert();

        //used this if only there is one unassigned employee as pre-conditon
        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "View Unassigned button is still clickable, indicating the employee is still unassigned"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move Employee tidak sesuai!"
        );
        softAssert.assertAll();

        logger.info("Test Completed Successfully: moveUnassignedEmployeeToRenamedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Existing Incumbent from one position to newly added Organization")
    public void moveExistingIncumbentToAnotherOrgTest() {
        logger.info("Starting test: moveExistingIncumbentToAnotherOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel() != null && !proposedFlow1Data.getLevel().isEmpty() ? proposedFlow1Data.getLevel() : "D3";
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        assignEmployeeHelper().moveIncumbentFromPosition(
                sourceOrg,
                sourcePosition,
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
        assignEmployeeHelper().closeManageIncumbentModal();

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position"
        );

        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionNotVisible(sourceOrg, sourcePosition, employeeIdentifier),
                "Employee is still displayed in the source position"
        );
        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move Existing Incumbent tidak sesuai!"
        );
        softAssert.assertAll();

        logger.info("Test Completed Successfully: moveExistingIncumbentToAnotherOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Incumbent as PJS (Secondary Assignment)")
    public void moveIncumbentAsPjsTest() {
        logger.info("Starting test: moveIncumbentAsPjsTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        assignEmployeeHelper().moveIncumbentFromPosition(
                sourceOrg,
                sourcePosition,
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                true // Set as PJS (Secondary Assignment)
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);
        CustomAssert.assertTrue(assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position as PJS"
        );

        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(sourceOrg, sourcePosition, employeeIdentifier),
                "Employee is not displayed in the source position after being moved as PJS"
        );
        assignEmployeeHelper().closeManageIncumbentModal();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Assign Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move PJS tidak sesuai!"
        );

        softAssert.assertAll(); // Assert all soft assertions
        logger.info("Test Completed Successfully: moveIncumbentAsPjsTest");
    }

//    TODO: ubah ntar dah
    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Move Incumbent with Mobility considered as YES")
    public void moveIncumbentWithMobilityYesTest() {
        logger.info("Starting test: moveIncumbentWithMobilityYesTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.MOVE, OrgMenuAction.MOVE);

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetLevel = proposedFlow1Data.getLevel();
        String targetJob = proposedFlow1Data.getJobName();
        String targetPosition = proposedFlow1Data.getJobName();

        assignEmployeeHelper().moveIncumbentFromPosition(
                sourceOrg,
                sourcePosition,
                employeeIdentifier,
                targetLevel,
                targetOrg,
                targetJob,
                targetPosition,
                currentAssignData.isMobility(), // Mobility = YES
                currentAssignData.getMovementCategory(),
                false
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);
        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee is not displayed in the target position after move with Mobility YES"
        );
        assignEmployeeHelper().closeManageIncumbentModal();

        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Move dengan Mobility YES tidak sesuai!"
        );
        softAssert.assertAll();

        logger.info("Test Completed Successfully: moveIncumbentWithMobilityYesTest");
    }
}
