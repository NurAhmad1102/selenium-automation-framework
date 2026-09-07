package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.factories.frontoffice.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.AssignEmployeePage;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.AddIncumbentModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.EmployeeMovementModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.ManageIncumbentModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.UnassignedEmployeeListModal;
import org.example.enums.frontoffice.manageOrganization.ConfirmAction;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ActionElementsPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ConfirmationActionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.ManageOrganizationExcelReader;
import org.example.model.frontoffice.OrganizationTestData;

public class AssignEmployeeHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final FrontOfficeMainPagesFactory mainPagesFactory;
    private final ManageOrganizationHelper manageOrganizationHelper;
    private final SetOrganizationHelper setOrganizationHelper;

    public AssignEmployeeHelper(FrontOfficeMainPagesFactory mainPagesFactory,
                                ManageOrganizationHelper manageOrganizationHelper,
                                SetOrganizationHelper setOrganizationHelper) {
        this.mainPagesFactory = mainPagesFactory;
        this.manageOrganizationHelper = manageOrganizationHelper;
        this.setOrganizationHelper = setOrganizationHelper;
    }

    // Lazy getters for Page and Modals
    public AssignEmployeePage assignEmployeePage() {
        return mainPagesFactory.manageOrganization().getAssignEmployeeFlow2Page();
    }

    public AddIncumbentModal addIncumbentModal() {
        return mainPagesFactory.manageOrganization().assignEmployeeModals().getAddIncumbentModal();
    }

    public EmployeeMovementModal employeeMovementModal() {
        return mainPagesFactory.manageOrganization().assignEmployeeModals().getEmployeeMovementModal();
    }

    public ManageIncumbentModal manageIncumbentModal() {
        return mainPagesFactory.manageOrganization().assignEmployeeModals().getManageIncumbentModal();
    }

    public UnassignedEmployeeListModal unassignedEmployeeListModal() {
        return mainPagesFactory.manageOrganization().assignEmployeeModals().getUnassignedEmployeeListModal();
    }

    public ConfirmationActionModal confirmationActionModal() {
        return mainPagesFactory.manageOrganization().commonModals().getConfirmationActionModal();
    }

    public SuccessModal successModal() {
        return mainPagesFactory.manageOrganization().commonModals().getSuccessModal();
    }

    public ActionElementsPage actionElementsPage() {
        return mainPagesFactory.manageOrganization().getActionElementsPage();
    }

    /**
     * Membaca data khusus dari Sheet 'ASSIGN_EMPLOYEE' berdasarkan TYPE ('UNASSIGNED', 'INSIDE', 'OUTSIDE')
     */
    public OrganizationTestData getAssignEmployeeData(String type) {
        logger.info("Reading ASSIGN_EMPLOYEE sheet data for TYPE: [{}]", type);
        return ManageOrganizationExcelReader.getDataByType(OrgSheetName.ASSIGN_EMPLOYEE, type);
    }

    /**
     * Membaca data khusus dari Sheet 'ASSIGN_EMPLOYEE' berdasarkan baris (0-indexed)
     */
    public OrganizationTestData getAssignEmployeeDataByRow(int rowIndex) {
        logger.info("Reading ASSIGN_EMPLOYEE sheet data for Row Index: [{}]", rowIndex);
        return ManageOrganizationExcelReader.getDataByRow(OrgSheetName.ASSIGN_EMPLOYEE, rowIndex);
    }

    public void closeManageIncumbentModal() {
        logger.info("Closing Manage Incumbent Modal");
        manageIncumbentModal().clickCloseButton();
    }

    public void closeUnassignedEmployeeListModal() {
        logger.info("Closing Unassigned Employee List Modal");
        unassignedEmployeeListModal().clickCloseButton();
    }

    /**
     * Reusable Helper: Pake ini buat Pre-Condition Flow 1 > Flow 2.
     * Data masih dari excel Manage_organization_data_test
     */
    public OrganizationTestData prepareFlow2ContextWithFlow1Precondition(OrgSheetName sheet, OrgMenuAction action) {
        logger.info("Phase: --- Running Pre-Condition Flow 1 ({}) for Flow 2 Context ---", action.getActionName());

        OrganizationTestData flow1Data = setOrganizationHelper.getTestData(sheet);
        manageOrganizationHelper.goToFlow1SetOrganization();

        switch (action) {
            case ADD_NEW:
                setOrganizationHelper.addNewOrganization(flow1Data);
                setOrganizationHelper.fillJobPosition(flow1Data, action);
                setOrganizationHelper.confirmAction(ConfirmAction.ADD);
                break;
            case RENAME:
                setOrganizationHelper.renameOrganization(flow1Data);
                setOrganizationHelper.fillJobPosition(flow1Data, action);
                setOrganizationHelper.confirmAction(ConfirmAction.RENAME);
                break;
            case MOVE:
                setOrganizationHelper.moveOrganization(flow1Data);
                setOrganizationHelper.fillJobPosition(flow1Data, action);
                setOrganizationHelper.confirmAction(ConfirmAction.MOVE);
                break;
            case MERGE:
                setOrganizationHelper.mergeOrganization(flow1Data);
                setOrganizationHelper.fillJobPosition(flow1Data, action);
                setOrganizationHelper.confirmAction(ConfirmAction.MERGE);
                break;
            case SPLIT:
                OrganizationTestData org2Data = flow1Data.createSecondarySplitData();
                setOrganizationHelper.splitOrganization(flow1Data, org2Data);
                setOrganizationHelper.fillJobPositionForSplit(flow1Data, org2Data);
                setOrganizationHelper.confirmAction(ConfirmAction.SPLIT);
                break;
            case REMOVE:
                setOrganizationHelper.removeOrganization(flow1Data.getPrimaryOrganization());
                setOrganizationHelper.confirmAction(ConfirmAction.REMOVE);
                break;
        }

        setOrganizationHelper.setParentPositionToNode(flow1Data.getJobName());
        manageOrganizationHelper.goToFlow2AssignEmployee();
        return flow1Data;
    }

    /**
     * get unassigned name index 0 UI unassigned list.
     */
    public String getFirstUnassignedEmployeeName() {
        logger.info("Step: Fetching First Unassigned Employee Name dynamically from UI");
        assignEmployeePage().clickViewUnassignedButton();
        String employeeName = unassignedEmployeeListModal().getFirstEmployeeName();
        logger.info("Get unassigned employee name from UI: [{}]", employeeName);
        return employeeName;
    }

    public void addIncumbent(String orgName, String positionName, String incumbentName, boolean isMobility, String category, boolean isPJS) {
        logger.info("Phase: --- Adding Incumbent: {} to Position: {} in Org: {} ---", incumbentName, positionName, orgName);
        assignEmployeePage().clickManageIncumbentOfNode(orgName);
        manageIncumbentModal().clickAddIncumbentButton(positionName);

        addIncumbentModal().selectIncumbentName(incumbentName);
        addIncumbentModal().selectMobility(isMobility);
        addIncumbentModal().selectMovementCategory(category);
        addIncumbentModal().setAsPJS(isPJS);
        addIncumbentModal().clickApplyButton();
    }

    public void addIncumbentsUntilNoDataFound(String orgName, String positionName, boolean isMobility, String category, boolean isPJS) {
        logger.info("Phase: --- Adding Incumbents to Position: [{}] in Org: [{}] until No Data Found ---", positionName, orgName);
        if (!manageIncumbentModal().isModalDisplayed()) {
            assignEmployeePage().clickManageIncumbentOfNode(orgName);
        }

        while (!manageIncumbentModal().isAddIncumbentButtonDisabled(positionName)) {
            manageIncumbentModal().clickAddIncumbentButton(positionName);
            addIncumbentModal().clickIncumbentNameDropdown();

            // if still there is unassigned employee, this button will be clickable
            if (!assignEmployeePage().isViewUnassignedButtonClickable()) {
                logger.info("No available incumbents / No data found in dropdown for position: [{}]. Finishing add incumbent process.", positionName);
                break;
            }

            addIncumbentModal().selectFirstAvailableIncumbent();
            addIncumbentModal().selectMobility(isMobility);
            addIncumbentModal().selectMovementCategory(category);
            addIncumbentModal().setAsPJS(isPJS);
            addIncumbentModal().clickApplyButton();
            confirmAction(ConfirmAction.ADD);
        }
    }

    public void addIncumbentsUntilNoDataFound(String orgName, String positionName, String category) {
        addIncumbentsUntilNoDataFound(orgName, positionName, true, category, false);
    }

    public void moveIncumbentFromUnassigned(String employeeName, String targetLevel, String targetOrgName, String targetJobName, String targetPositionName, boolean isMobility, String category, boolean isPJS) {
        logger.info("Phase: --- Moving Employee from Unassigned: {} to Target Org: {} ---", employeeName, targetOrgName);
        assignEmployeePage().clickViewUnassignedButton();
        unassignedEmployeeListModal().clickMoveEmployee(employeeName);

        employeeMovementModal().selectOrganizationLevel(targetLevel);
        employeeMovementModal().selectOrganizationName(targetOrgName);
        employeeMovementModal().selectJobName(targetJobName);
        employeeMovementModal().selectPositionName(targetPositionName);
        employeeMovementModal().selectMobility(isMobility);
        employeeMovementModal().selectMovementCategory(category);
        employeeMovementModal().setAsPJS(isPJS);
        employeeMovementModal().clickApplyButton();
    }

    public void moveIncumbentFromPosition(String sourceOrgName, String sourcePositionName, String employeeNameOrId, String targetLevel, String targetOrgName, String targetJobName, String targetPositionName, boolean isMobility, String category, boolean isPJS) {
        logger.info("Phase: --- Moving Employee: {} from Org: {} Position: {} ---", employeeNameOrId, sourceOrgName, sourcePositionName);
        assignEmployeePage().clickManageIncumbentOfNode(sourceOrgName);
        manageIncumbentModal().expandPositionAccordion(sourcePositionName);
        manageIncumbentModal().clickIncumbentKebabMenu(employeeNameOrId);
        manageIncumbentModal().selectKebabAction("Move");

        employeeMovementModal().selectOrganizationLevel(targetLevel);
        employeeMovementModal().selectOrganizationName(targetOrgName);
        employeeMovementModal().selectJobName(targetJobName);
        employeeMovementModal().selectPositionName(targetPositionName);
        employeeMovementModal().selectMobility(isMobility);
        employeeMovementModal().selectMovementCategory(category);
        employeeMovementModal().setAsPJS(isPJS);
        employeeMovementModal().clickApplyButton();
    }

    public boolean moveIncumbentIntoFTEFullPosition(String employeeName, String targetLevel, String targetOrgName, String targetJobName) {
        logger.info("Phase: --- Moving Employee from Unassigned: {} to Target Org: {} ---", employeeName, targetOrgName);
        assignEmployeePage().clickViewUnassignedButton();
        unassignedEmployeeListModal().clickMoveEmployee(employeeName);

        employeeMovementModal().selectOrganizationLevel(targetLevel);
        employeeMovementModal().selectOrganizationName(targetOrgName);
        employeeMovementModal().selectJobName(targetJobName);
        employeeMovementModal().clickPositionNameDropdown();

        return employeeMovementModal().isTextDisplayed("No data found");
    }

    public boolean moveIncumbentIntoFTEFullPosition(String sourceOrgName, String sourcePositionName, String employeeNameOrId, String targetLevel, String targetOrgName, String targetJobName) {
        logger.info("Phase: --- Moving Employee: {} from Org: {} Position: {} ---", employeeNameOrId, sourceOrgName, sourcePositionName);
        assignEmployeePage().clickManageIncumbentOfNode(sourceOrgName);
        manageIncumbentModal().expandPositionAccordion(sourcePositionName);
        manageIncumbentModal().clickIncumbentKebabMenu(employeeNameOrId);
        manageIncumbentModal().selectKebabAction("Move");

        employeeMovementModal().selectOrganizationLevel(targetLevel);
        employeeMovementModal().selectOrganizationName(targetOrgName);
        employeeMovementModal().selectJobName(targetJobName);
        employeeMovementModal().clickPositionNameDropdown();
        return employeeMovementModal().isTextDisplayed("No data found");
    }

    public void removeIncumbent(String orgName, String positionName, String employeeNameOrId) {
        logger.info("Phase: --- Removing Incumbent: {} from Org: {} Position: {} ---", employeeNameOrId, orgName, positionName);
        assignEmployeePage().clickManageIncumbentOfNode(orgName);
        manageIncumbentModal().expandPositionAccordion(positionName);
        manageIncumbentModal().clickIncumbentKebabMenu(employeeNameOrId);
        manageIncumbentModal().selectKebabAction("Remove");
    }

    public void confirmAction(ConfirmAction action) {
        logger.info("Step: Confirming action: {}", action.getActionName());
        confirmationActionModal().clickYesButton();
        successModal().clickContinueButton();
    }
}