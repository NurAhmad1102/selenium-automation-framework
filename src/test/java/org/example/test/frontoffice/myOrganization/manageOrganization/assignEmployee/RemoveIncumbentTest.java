package org.example.test.frontoffice.myOrganization.manageOrganization.assignEmployee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.annotations.Test;

public class RemoveIncumbentTest extends AssignEmployeeBaseTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Remove Primary Incumbent and validate employee becomes Unassigned")
    public void removePrimaryIncumbentTest() {
        logger.info("Starting test: removePrimaryIncumbentTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");
        manageOrganizationHelper().goToFlow1SetOrganization();
        manageOrganizationHelper().goToFlow2AssignEmployee();

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().removeIncumbent(sourceOrg, sourcePosition, employeeIdentifier);
        assignEmployeeHelper().confirmAction(ConfirmAction.REMOVE);

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionNotVisible(sourceOrg, sourcePosition, employeeIdentifier),
                "Employee still exist in existing position");

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInUnassignedList(employeeIdentifier),
                "Employee does not exist in unassigned list"
        );

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Remove Employee", employeeIdentifier, sourceOrg),
                "Audit Log Employee Assignment untuk Remove Incumbent tidak sesuai!"
        );

        logger.info("Test Completed Successfully: removePrimaryIncumbentTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Remove PJS (Secondary Assignment) without affecting Primary Assignment")
    public void removeNewSecondaryPjsIncumbentTest() {
        logger.info("Starting test: removeSecondaryPjsIncumbentTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                true // PJS
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        assignEmployeeHelper().removeIncumbent(targetOrg, targetPosition, employeeIdentifier);
        assignEmployeeHelper().confirmAction(ConfirmAction.REMOVE);

        // di position pjs yang di add baru akan hilang
        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionNotVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee still exist in new position, after removing him/her as pjs from this position"
        );

        // di position primary yang exisiting tetap ada
        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(currentAssignData.getCurrentOrg(), currentAssignData.getCurrentPosition(), employeeIdentifier),
                "Employee does not exist in existing position"
        );

        CustomAssert.assertFalse(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Remove Incumbent", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Remove PJS tidak sesuai, log tetap masuk!"
        );

        logger.info("Test Completed Successfully: removeSecondaryPjsIncumbentTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Remove PJS (Secondary Assignment) without affecting Primary Assignment")
    public void removeExisitingSecondaryPjsIncumbentTest() {
        logger.info("Starting test: removeSecondaryPjsIncumbentTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("PJS");

        String targetOrg = currentAssignData.getCurrentOrg();
        String targetPosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

//        to navigate
        manageOrganizationHelper().goToFlow1SetOrganization();
        manageOrganizationHelper().goToFlow2AssignEmployee();

        assignEmployeeHelper().removeIncumbent(targetOrg, targetPosition, employeeIdentifier);
        assignEmployeeHelper().confirmAction(ConfirmAction.REMOVE);

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionNotVisible(targetOrg, targetPosition, employeeIdentifier),
                "Employee still exist in existing position as PJS"
        );

        CustomAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Remove Incumbent", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Remove PJS tidak sesuai!"
        );

        logger.info("Test Completed Successfully: removeSecondaryPjsIncumbentTest");
//        TODO: cek saat pjs diremove, di position lama: ilangan, masuk ke log
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify that PJS Employee only has Remove option and cannot be Moved (BR-04)")
    public void verifyPjsCannotBeMovedTest() {
        logger.info("Starting test: verifyPjsCannotBeMovedTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("PJS");

        String targetOrg = currentAssignData.getCurrentOrg();
        String targetPosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

//        to navigate
        manageOrganizationHelper().goToFlow1SetOrganization();
        manageOrganizationHelper().goToFlow2AssignEmployee();

        assignEmployeeHelper().assignEmployeePage().clickManageIncumbentOfNode(targetOrg);
        assignEmployeeHelper().manageIncumbentModal().expandPositionAccordion(targetPosition);
        assignEmployeeHelper().manageIncumbentModal().clickIncumbentKebabMenu(employeeIdentifier);

        CustomAssert.assertFalse(
                assignEmployeeHelper().manageIncumbentModal().isKebabActionDisplayed("Move"),
                "Karyawan berstatus PJS tidak boleh memiliki opsi 'Move'!"
        );

        assignEmployeeHelper().manageIncumbentModal().clickCloseButton();
        logger.info("Test Completed Successfully: verifyPjsCannotBeMovedTest");
    }
}
