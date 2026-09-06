package org.example.test.frontoffice.myOrganization.manageOrganization.assignEmployee;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.enums.frontoffice.manageOrganization.AssignmentTag;
import org.example.enums.frontoffice.manageOrganization.ConfirmAction;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.example.utils.CustomSoftAssert;
import org.testng.annotations.Test;

public class AddIncumbentTest extends AssignEmployeeBaseTest {
    private final Logger logger = LogManager.getLogger(getClass());

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position in Added Organization using Employee Removed from Existing Position")
    public void addIncumbentFromUnassignedAfterRemovingIntoAddedOrgTest() {
        logger.info("Starting test: addIncumbentFromUnassignedAfterRemovingIntoAddedOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().removeIncumbent(
                sourceOrg,
                sourcePosition,
                employeeIdentifier
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.REMOVE);

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.MOVE),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        assignEmployeeHelper().closeManageIncumbentModal();
        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionNotVisible(sourceOrg, sourcePosition, employeeIdentifier),
                "Employee " + employeeIdentifier +
                        " masih muncul di Manage Incumbent di position: " + sourcePosition +
                        " pada org " + sourceOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Incumbent setelah remove tidak sesuai!"
        );

        softAssert.assertAll();

        logger.info("Test Completed Successfully: addIncumbentFromUnassignedAfterRemovingIntoAddedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Change Assignment Type to PJS in the same Organization")
    public void changeAssignmentTypeInTheSameOrgPrimaryToBePjsTest() {
        logger.info("Starting test: changeAssignmentTypeInTheSameOrgPrimaryToBePjsTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

        manageOrganizationHelper().goToFlow1SetOrganization();
        manageOrganizationHelper().goToFlow2AssignEmployee();

        assignEmployeeHelper().removeIncumbent(
                sourceOrg,
                sourcePosition,
                employeeIdentifier
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.REMOVE);

        assignEmployeeHelper().addIncumbent(
                sourceOrg,
                sourcePosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                true // PJS Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(sourceOrg, sourcePosition, employeeIdentifier, AssignmentTag.PJS_NEW),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent di position: " + sourcePosition +
                        " pada org " + sourceOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInUnassignedListVisible(employeeIdentifier),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Unassigned List!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Assign Employee", employeeIdentifier, sourceOrg),
                "Audit Log Employee Assignment untuk Change Assignment Type tidak sesuai!"
        );

        softAssert.assertAll();

        logger.info("Test Completed Successfully: changeAssignmentTypeInTheSameOrgPrimaryToBePjsTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Change Assignment Type to Primary in the same Organization")
    public void changeAssignmentTypeInTheSameOrgPjsToBePrimaryTest() {
        logger.info("Starting test: changeAssignmentTypeInTheSameOrgPjsToBePrimaryTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("PJS");

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();
        String employeeIdentifier = currentAssignData.getNip();

        manageOrganizationHelper().goToFlow1SetOrganization();
        manageOrganizationHelper().goToFlow2AssignEmployee();

        assignEmployeeHelper().removeIncumbent(
                sourceOrg,
                sourcePosition,
                employeeIdentifier
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.REMOVE);

        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "There is no incumbent. View Button should be disabled!"
        );

        assignEmployeeHelper().addIncumbent(
                sourceOrg,
                sourcePosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.MOVE);

        //"Move" cause employee has another assignment in the same org tree, if no "New"
        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(sourceOrg, sourcePosition, employeeIdentifier, AssignmentTag.MOVE),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent di position: " + sourcePosition +
                        " pada org " + sourceOrg + "dengan Tag "+ AssignmentTag.MOVE + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, sourceOrg),
                "Audit Log Employee Assignment untuk Change Assignment Type tidak sesuai!"
        );

        softAssert.assertAll();

        logger.info("Test Completed Successfully: changeAssignmentTypeInTheSameOrgPjsToBePrimaryTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position using Existing Employee")
    public void addIncumbentFromExistingEmployeeTest() {
        logger.info("Starting test: addIncumbentFromExistingEmployeeTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.MOVE),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionNotVisible(sourceOrg, sourcePosition, employeeIdentifier),
                "Employee " + employeeIdentifier +
                        " masih muncul di Manage Incumbent di position: " + sourcePosition +
                        " pada org " + sourceOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Existing Incumbent tidak sesuai!"
        );

        softAssert.assertAll();
        logger.info("Test Completed Successfully: addIncumbentFromExistingEmployeeTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position using Existing Employee from Outside Current Organization Tree")
    public void addIncumbentFromOutsideOrganizationTreeTest() {
        logger.info("Starting test: addIncumbentFromOutsideOrganizationTreeTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("OUTSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.NEW),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Assign Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Existing Incumbent tidak sesuai!"
        );

        softAssert.assertAll();
        logger.info("Test Completed Successfully: addIncumbentFromOutsideOrganizationTreeTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent as PJS (Secondary Assignment)")
    public void addIncumbentAsPjsTest() {
        logger.info("Starting test: addIncumbentAsPjsTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.ADD_NEW, OrgMenuAction.ADD_NEW);

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                true // Set as PJS (Secondary Assignment)
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(sourceOrg, sourcePosition, employeeIdentifier, AssignmentTag.NONE),
                "Incumbent tidak muncul di primary position: " + sourcePosition +
                        " pada org: " + sourceOrg
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.PJS_NEW),
                "Incumbent tidak muncul as PJS di target position: " + targetPosition +
                        " pada org: " + targetOrg
        );

        assignEmployeeHelper().closeManageIncumbentModal();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Assign Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add PJS tidak sesuai!"
        );

        softAssert.assertAll();
        logger.info("Test Completed Successfully: addIncumbentAsPjsTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent button is Disabled when Position FTE is full (2/2)")
    public void addIncumbentWhenFteFullNegativeTest() {
        logger.info("Starting test: addIncumbentWhenFteFullNegativeTest");

        OrganizationTestData currentData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        String sourceOrg = currentData.getCurrentOrg();
        String sourcePosition = currentData.getCurrentPosition();

        manageOrganizationHelper().goToFlow1SetOrganization();
        manageOrganizationHelper().goToFlow2AssignEmployee();
        assignEmployeeHelper().assignEmployeePage().clickManageIncumbentOfNode(sourceOrg);

        CustomAssert.assertTrue(
                assignEmployeeHelper().manageIncumbentModal().isAddIncumbentButtonDisabled(sourcePosition),
                "Tombol Add Incumbent di position: " + sourcePosition +
                        " pada org: " + sourceOrg +
                        " seharusnya Disabled saat kapasitas FTE sudah penuh!"
        );

        logger.info("Test Completed Successfully: addIncumbentWhenFteFullNegativeTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position in Renamed Organization using Unassigned Employee")
    public void addIncumbentFromUnassignedToRenamedOrgTest() {
        logger.info("Starting test: addIncumbentFromUnassignedToRenamedOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.RENAME, OrgMenuAction.RENAME);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.MOVE),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "Tombol View Unassigned seharusnya disable ketika tidak ada Unassigned Employee!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Incumbent ke Renamed Org tidak sesuai!"
        );

        softAssert.assertAll();

        logger.info("Test Completed Successfully: addIncumbentFromUnassignedToRenamedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position in Moved Organization using Unassigned Employee")
    public void addIncumbentFromUnassignedToMovedOrgTest() {
        logger.info("Starting test: addIncumbentFromUnassignedToMovedOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.MOVE, OrgMenuAction.MOVE);

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.MOVE),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "Tombol View Unassigned seharusnya disable ketika tidak ada Unassigned Employee!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Incumbent ke Moved Org tidak sesuai!"
        );

        softAssert.assertAll();

        logger.info("Test Completed Successfully: addIncumbentFromUnassignedToMovedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position in Merged Organization using Unassigned Employee")
    public void addIncumbentFromUnassignedToMergedOrgTest() {
        logger.info("Starting test: addIncumbentFromUnassignedToMergedOrgTest");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.MERGE, OrgMenuAction.MERGE);
        String employeeIdentifier = assignEmployeeHelper().getFirstUnassignedEmployeeName();
        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();

        String[] employee = employeeIdentifier.split(" - ");
        String employeeName = employee[0];

        assignEmployeeHelper().closeUnassignedEmployeeListModal();
        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeName,
                true,
                "Change Position",
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeName, AssignmentTag.MOVE),
                "Employee " + employeeName +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInUnassignedListNotVisible(employeeName),
                "Employee " + employeeName +
                        " masih muncul di Unassigned List!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Incumbent ke Merged Org tidak sesuai!"
        );

        softAssert.assertAll();
        logger.info("Test Completed Successfully: addIncumbentFromUnassignedToMergedOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent to Open Position in Split Organization using Unassigned Employee")
    public void addIncumbentFromUnassignedToSplitOrgTest() {
        logger.info("Starting test: addIncumbentFromUnassignedToSplitOrgTest");

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("UNASSIGNED");

        OrganizationTestData proposedFlow1Data = assignEmployeeHelper().prepareFlow2ContextWithFlow1Precondition(OrgSheetName.SPLIT, OrgMenuAction.SPLIT);

        String targetOrg = proposedFlow1Data.getNewOrganizationName1();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary Assignment
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.MOVE),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        softAssert.assertFalse(
                assignEmployeeHelper().isViewUnassignedButtonClickable(),
                "Tombol View Unassigned seharusnya disable ketika tidak ada Unassigned Employee!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Incumbent ke Split Org tidak sesuai!"
        );

        softAssert.assertAll();

        logger.info("Test Completed Successfully: addIncumbentFromUnassignedToSplitOrgTest");
    }

    @Test(groups = {"smoke", "assignEmployee"}, description = "Verify Add Incumbent into Position with New Job")
    public void addIncumbentIntoPositionWithNewJobTest() {
        logger.info("Starting test: addIncumbentIntoPositionWithNewJobTest");

        OrganizationTestData proposedFlow1Data = setOrganizationHelper().getTestData(OrgSheetName.ADD_NEW);
//        Ubah nama job dari excel jadi job baru
        proposedFlow1Data.setJobName(createNewJobName);

        OrganizationTestData currentAssignData = assignEmployeeHelper().getAssignEmployeeData("INSIDE");

        manageOrganizationHelper().goToFlow1SetOrganization();
        setOrganizationHelper().addNewOrganization(proposedFlow1Data);
        setOrganizationHelper().fillJobPositionWithNewJob(
                proposedFlow1Data,
                OrgMenuAction.ADD_NEW,
                proposedFlow1Data.getJobName(),
                true);
        setOrganizationHelper().confirmAction(ConfirmAction.ADD);

        manageOrganizationHelper().goToFlow2AssignEmployee();

        String sourceOrg = currentAssignData.getCurrentOrg();
        String sourcePosition = currentAssignData.getCurrentPosition();

        String targetOrg = proposedFlow1Data.getNewOrganizationName();
        String targetPosition = proposedFlow1Data.getJobName();
        String employeeIdentifier = currentAssignData.getNip();

        assignEmployeeHelper().addIncumbent(
                targetOrg,
                targetPosition,
                employeeIdentifier,
                currentAssignData.isMobility(),
                currentAssignData.getMovementCategory(),
                false // Primary
        );
        assignEmployeeHelper().confirmAction(ConfirmAction.ADD);

        CustomSoftAssert softAssert = new CustomSoftAssert();

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionVisible(targetOrg, targetPosition, employeeIdentifier, AssignmentTag.MOVE),
                "Employee " + employeeIdentifier +
                        " tidak muncul di Manage Incumbent setelah Add Incumbent di position: " + targetPosition +
                        " pada org " + targetOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeInPositionNotVisible(sourceOrg, sourcePosition, employeeIdentifier),
                "Employee " + employeeIdentifier +
                        " masih muncul di Manage Incumbent di position: " + sourcePosition +
                        " pada org " + sourceOrg + "!"
        );

        softAssert.assertTrue(
                assignEmployeeHelper().verifyEmployeeAssignmentLog("Move Employee", employeeIdentifier, targetOrg),
                "Audit Log Employee Assignment untuk Add Existing Incumbent tidak sesuai!"
        );

        softAssert.assertAll();
        logger.info("Test Completed Successfully: addIncumbentFromExistingEmployeeTest");
    }
}
