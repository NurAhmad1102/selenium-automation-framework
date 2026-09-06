package org.example.test.frontoffice.myOrganization.manageOrganization.organizationChangesJustification;

import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomSoftAssert;
import org.testng.annotations.Test;

public class OrganizationChangesJustificationTest extends ManageOrganizationBaseTest {

    @Test(groups = {"smoke"}, description = "End-to-End Validasi Flow 3 untuk Action ADD NEW (New Job, New Position, Current & Proposed Structure, Proposed Employee Assignment)")
    public void validateAddOrganizationJustificationTest() {
        logger.info("Starting End-to-End Test: validateAddOrganizationJustificationTest");

        OrganizationTestData flow1Data = setOrganizationHelper().getTestData(OrgSheetName.ADD_NEW);
        flow1Data.setJobName(createNewJobName);
        OrganizationTestData flow2Data = assignEmployeeHelper().getAssignEmployeeData("OUTSIDE");

        manageOrganizationHelper().goToFlow1SetOrganization();
        setOrganizationHelper().addNewOrganization(flow1Data);
        setOrganizationHelper().fillJobPositionWithNewJob(
                flow1Data,
                OrgMenuAction.ADD_NEW,
                flow1Data.getJobName(),
                true);
        setOrganizationHelper().confirmAction(ConfirmAction.ADD);

        manageOrganizationHelper().goToFlow2AssignEmployee();
        String targetOrg = flow1Data.getNewOrganizationName();
        String targetPosition = flow1Data.getJobName();
        String employeeIdentifier = flow2Data.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                flow2Data.isMobility(),
                flow2Data.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        manageOrganizationHelper().goToFlow3OrgChangesJustification();
        justificationHelper().validateAddOrganizationJustificationFlow(flow1Data, flow2Data, true);

        logger.info("End-to-End Test Passed: validateAddOrganizationJustificationTest");
    }

    @Test(description = "End-to-End Validasi Flow 3 untuk Action MERGE Organization")
    public void validateMergeOrganizationJustificationTest() {
        logger.info("Starting End-to-End Test: validateMergeOrganizationJustificationTest");

        OrganizationTestData flow1Data = setOrganizationHelper().getTestData(OrgSheetName.MERGE);
        flow1Data.setFteNumbers("8");
        OrganizationTestData flow2Data = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        manageOrganizationHelper().goToFlow1SetOrganization();
        setOrganizationHelper().mergeOrganization(flow1Data);
        setOrganizationHelper().fillJobPosition(flow1Data, OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);
        setOrganizationHelper().setParentPositionToNode(flow1Data.getJobName());

        manageOrganizationHelper().goToFlow2AssignEmployee();
        String targetOrg = flow1Data.getNewOrganizationName();
        String targetPosition = flow1Data.getJobName();
        String employeeIdentifier = flow2Data.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                flow2Data.isMobility(),
                flow2Data.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

//        add incumbent for the rest
        assignEmployeeHelper().addIncumbentsUntilNoDataFound(
                targetOrg,
                targetPosition,
                flow2Data.getMovementCategory()
        );

        manageOrganizationHelper().goToFlow3OrgChangesJustification();
        justificationHelper().validateMergeOrganizationJustificationFlow(flow1Data, flow2Data, false);

        logger.info("End-to-End Test Passed: validateMergeOrganizationJustificationTest");
    }

    @Test(description = "End-to-End Validasi Flow 3 untuk Action SPLIT Organization")
    public void validateSplitOrganizationJustificationTest() {
        logger.info("Starting End-to-End Test: validateSplitOrganizationJustificationTest");

        OrganizationTestData org1Data = setOrganizationHelper().getTestData(OrgSheetName.SPLIT);
        OrganizationTestData org2Data = org1Data.createSecondarySplitData();
        org1Data.setNewOrganizationName(org1Data.getNewOrganizationName1());
        org2Data.setNewOrganizationName(org2Data.getNewOrganizationName2());

        OrganizationTestData flow2Data = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        manageOrganizationHelper().goToFlow1SetOrganization();
        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        setOrganizationHelper().fillJobPositionForSplit(org1Data, org2Data);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);
        setOrganizationHelper().setParentPositionToNode(org1Data.getJobName());

        manageOrganizationHelper().goToFlow2AssignEmployee();
        String targetOrg = org1Data.getNewOrganizationName1();
        String targetPosition = org1Data.getJobName();
        String employeeIdentifier = flow2Data.getNip();

        assignEmployeeHelper().moveIncumbentFromUnassigned(
                employeeIdentifier,
                org2Data.getLevel(),
                org2Data.getNewOrganizationName2(),
                org2Data.getJobName2(),
                org2Data.getJobName2(),
                flow2Data.isMobility(),
                flow2Data.getMovementCategory(),
                true
        );

        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                flow2Data.isMobility(),
                flow2Data.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        manageOrganizationHelper().goToFlow3OrgChangesJustification();
        CustomSoftAssert softAssert = new CustomSoftAssert();

//        verify position
        justificationHelper().validateNewPositionDetails(softAssert, org1Data.getJobName(), org1Data, OrgMenuAction.SPLIT);
        justificationHelper().validateNewPositionDetails(softAssert, org2Data.getJobName(), org2Data, OrgMenuAction.SPLIT);

//        verify org
        justificationHelper().verifySplitOrganizationStructureInJustification(
                softAssert,
                org1Data.getPrimaryOrganization(),
                org1Data.getNewOrganizationName1(),
                org2Data.getNewOrganizationName2());

//        verify proposed employee assigm
        justificationHelper().validateProposedEmployeeAssignmentSection(softAssert, org1Data, flow2Data, false);
        justificationHelper().validateProposedEmployeeAssignmentSection(softAssert, org2Data, flow2Data, true);
        softAssert.assertAll();
        logger.info("End-to-End Test Passed: validateSplitOrganizationJustificationTest");
    }

    @Test(description = "End-to-End Validasi Flow 3 untuk Action RENAME Organization")
    public void validateRenameOrganizationJustificationTest() {
        logger.info("Starting End-to-End Test: validateRenameOrganizationJustificationTest");

        OrganizationTestData flow1Data = setOrganizationHelper().getTestData(OrgSheetName.RENAME);
        OrganizationTestData flow2Data = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        manageOrganizationHelper().goToFlow1SetOrganization();
        setOrganizationHelper().renameOrganization(flow1Data);
        setOrganizationHelper().fillJobPosition(flow1Data, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);
        setOrganizationHelper().setParentPositionToNode(flow1Data.getJobName());

        manageOrganizationHelper().goToFlow2AssignEmployee();
        String targetOrg = flow1Data.getNewOrganizationName();
        String targetPosition = flow1Data.getJobName();
        String employeeIdentifier = flow2Data.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                flow2Data.isMobility(),
                flow2Data.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        manageOrganizationHelper().goToFlow3OrgChangesJustification();
        justificationHelper().validateRenameOrganizationJustificationFlow(flow1Data, flow2Data, false);

        logger.info("End-to-End Test Passed: validateRenameOrganizationJustificationTest");
    }

    @Test(description = "End-to-End Validasi Flow 3 untuk Action Move Organization")
    public void validateMoveOrganizationJustificationTest() {
        logger.info("Starting End-to-End Test: validateMoveOrganizationJustificationTest");

        OrganizationTestData flow1Data = setOrganizationHelper().getTestData(OrgSheetName.MOVE);
        OrganizationTestData flow2Data = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        manageOrganizationHelper().goToFlow1SetOrganization();
        setOrganizationHelper().moveOrganization(flow1Data);
        setOrganizationHelper().fillJobPosition(flow1Data, OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);
        setOrganizationHelper().setParentPositionToNode(flow1Data.getJobName());

        manageOrganizationHelper().goToFlow2AssignEmployee();
        String targetOrg = flow1Data.getNewOrganizationName();
        String targetPosition = flow1Data.getJobName();
        String employeeIdentifier = flow2Data.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                flow2Data.isMobility(),
                flow2Data.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        manageOrganizationHelper().goToFlow3OrgChangesJustification();
        justificationHelper().validateMoveOrganizationJustificationFlow(flow1Data, flow2Data, false);

        logger.info("End-to-End Test Passed: validateMoveOrganizationJustificationTest");
    }

    @Test(description = "End-to-End Validasi Flow 3 untuk Action REMOVE Organization")
    public void validateRemoveOrganizationJustificationTest() {
        logger.info("Starting End-to-End Test: validateRemoveOrganizationJustificationTest");

        // pakai data PJS agar tidak perlu assign unassigned employee
        OrganizationTestData flow2Data = assignEmployeeHelper().getAssignEmployeeData("PJS");

        String organizationToRemove = flow2Data.getCurrentOrg();

        manageOrganizationHelper().goToFlow1SetOrganization();
        setOrganizationHelper().removeOrganization(organizationToRemove);
        setOrganizationHelper().confirmAction(ConfirmAction.REMOVE);

        manageOrganizationHelper().goToFlow2AssignEmployee();
        manageOrganizationHelper().goToFlow3OrgChangesJustification();

        CustomSoftAssert softAssert = new CustomSoftAssert();
        justificationHelper().verifyRemoveOrganizationStructureInJustification(softAssert, organizationToRemove);
        softAssert.assertAll();
        logger.info("End-to-End Test Passed: validateRemoveOrganizationJustificationTest");
    }
}
