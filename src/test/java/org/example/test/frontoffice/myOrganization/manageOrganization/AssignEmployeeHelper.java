package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.AssignEmployeePage;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.AddIncumbentModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.EmployeeMovementModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.ManageIncumbentModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.UnassignedEmployeeListModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ActionElementsPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgSheetName;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ChangesLogModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ConfirmationActionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.ManageOrganizationExcelReader;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;

public class AssignEmployeeHelper {
    private final Logger logger = LogManager.getLogger(getClass());

    private final AssignEmployeePage assignEmployeePage;
    private final AddIncumbentModal addIncumbentModal;
    private final EmployeeMovementModal employeeMovementModal;
    private final ManageIncumbentModal manageIncumbentModal;
    private final UnassignedEmployeeListModal unassignedEmployeeListModal;
    private final ConfirmationActionModal confirmationActionModal;
    private final SuccessModal successModal;
    private final ChangesLogModal changesLogModal;
    private final ActionElementsPage actionElementsPage;

    private final ManageOrganizationHelper manageOrganizationHelper;
    private final SetOrganizationHelper setOrganizationHelper;

    public AssignEmployeeHelper(FrontOfficeMainPagesFactory mainPagesFactory,
                                ManageOrganizationHelper manageOrganizationHelper,
                                SetOrganizationHelper setOrganizationHelper) {
        this.assignEmployeePage = mainPagesFactory.manageOrganization().getAssignEmployeeFlow2Page();
        this.addIncumbentModal = mainPagesFactory.manageOrganization().assignEmployeeModals().getAddIncumbentModal();
        this.employeeMovementModal = mainPagesFactory.manageOrganization().assignEmployeeModals().getEmployeeMovementModal();
        this.manageIncumbentModal = mainPagesFactory.manageOrganization().assignEmployeeModals().getManageIncumbentModal();
        this.unassignedEmployeeListModal = mainPagesFactory.manageOrganization().assignEmployeeModals().getUnassignedEmployeeListModal();
        this.confirmationActionModal = mainPagesFactory.manageOrganization().modals().getConfirmationActionModal();
        this.successModal = mainPagesFactory.manageOrganization().modals().getSuccessModal();
        this.changesLogModal = mainPagesFactory.manageOrganization().modals().getChangesLogModal();
        this.actionElementsPage = mainPagesFactory.manageOrganization().getActionElementsPage();

        this.manageOrganizationHelper = manageOrganizationHelper;
        this.setOrganizationHelper = setOrganizationHelper;
    }

    public AssignEmployeePage assignEmployeePage() {
        return assignEmployeePage;
    }

    public AddIncumbentModal addIncumbentModal() {
        return addIncumbentModal;
    }

    public EmployeeMovementModal employeeMovementModal() {
        return employeeMovementModal;
    }

    public ManageIncumbentModal manageIncumbentModal() {
        return manageIncumbentModal;
    }

    public UnassignedEmployeeListModal unassignedEmployeeListModal() {
        return unassignedEmployeeListModal;
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

    /**
     * Reusable Helper: Pake ini baut Pre-Condition Flow 1 > Flow 2.
     * Data masih dari excel Manage_orgaization_data_test
     */
    public OrganizationTestData prepareFlow2ContextWithFlow1Precondition(OrgSheetName sheet, OrgMenuAction action) {
        logger.info("Phase: --- Running Pre-Condition Flow 1 ({}) for Flow 2 Context ---", action.getActionName());

        OrganizationTestData flow1Data = setOrganizationHelper.getTestData(sheet);
        manageOrganizationHelper.goToFlow1();

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
                OrganizationTestData org2Data = new OrganizationTestData(flow1Data);
                OrganizationTestData jobPositionData = setOrganizationHelper.getTestData(OrgSheetName.JOB_POSITION);
                if (jobPositionData != null) {
                    String targetJob = (jobPositionData.getJob() != null && !jobPositionData.getJob().trim().isEmpty())
                            ? jobPositionData.getJob()
                            : jobPositionData.getJobName();
                    if (targetJob != null && !targetJob.trim().isEmpty()) {
                        org2Data.setJobName(targetJob);
                        org2Data.setJob(targetJob);
                    }
                }
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

        logger.info("Step: Navigating from Flow 1 Canvas to Flow 2 (Assign Employee)");
        actionElementsPage.clickNextButton();
        setOrganizationHelper.nextToAssignEmployee();
        actionElementsPage.clickYesButtonPopUp();

        return flow1Data;
    }

    /**
     * get unassigned name index 0 UI unassigned list.
     */
    public String getFirstUnassignedEmployeeName() {
        logger.info("Step: Fetching First Unassigned Employee Name dynamically from UI");
        assignEmployeePage.clickViewUnassignedButton();
        String employeeName = unassignedEmployeeListModal.getFirstEmployeeName();
        unassignedEmployeeListModal.clickCloseButton();
        logger.info("Dynamically retrieved Unassigned Employee: [{}]", employeeName);
        return employeeName;
    }

    public void addIncumbent(String orgName, String positionName, String incumbentName, boolean isMobility, String category, boolean isPJS) {
        logger.info("Phase: --- Adding Incumbent: {} to Position: {} in Org: {} ---", incumbentName, positionName, orgName);
        assignEmployeePage.clickManageIncumbentOfNode(orgName);
        manageIncumbentModal.clickAddIncumbentButton(positionName);

        addIncumbentModal.selectIncumbentName(incumbentName);
        addIncumbentModal.selectMobility(isMobility);
        addIncumbentModal.selectMovementCategory(category);
        addIncumbentModal.setAsPJS(isPJS);
        addIncumbentModal.clickApplyButton();
    }

    public void moveIncumbentFromUnassigned(String employeeName, String targetLevel, String targetOrgName, String targetJobName, String targetPositionName, boolean isMobility, String category, boolean isPJS) {
        logger.info("Phase: --- Moving Employee from Unassigned: {} to Target Org: {} ---", employeeName, targetOrgName);
        assignEmployeePage.clickViewUnassignedButton();
        unassignedEmployeeListModal.clickMoveEmployee(employeeName);

        employeeMovementModal.selectOrganizationLevel(targetLevel);
        employeeMovementModal.selectOrganizationName(targetOrgName);
        employeeMovementModal.selectJobName(targetJobName);
        employeeMovementModal.selectPositionName(targetPositionName);
        employeeMovementModal.selectMobility(isMobility);
        employeeMovementModal.selectMovementCategory(category);
        employeeMovementModal.setAsPJS(isPJS);
        employeeMovementModal.clickApplyButton();
    }

    public void moveIncumbentFromPosition(String sourceOrgName, String positionName, String employeeNameOrId, String targetLevel, String targetOrgName, String targetJobName, String targetPositionName, boolean isMobility, String category, boolean isPJS) {
        logger.info("Phase: --- Moving Employee: {} from Org: {} Position: {} ---", employeeNameOrId, sourceOrgName, positionName);
        assignEmployeePage.clickManageIncumbentOfNode(sourceOrgName);
        manageIncumbentModal.expandPositionAccordion(positionName);
        manageIncumbentModal.clickIncumbentKebabMenu(employeeNameOrId);
        manageIncumbentModal.selectKebabAction("Move");

        employeeMovementModal.selectOrganizationLevel(targetLevel);
        employeeMovementModal.selectOrganizationName(targetOrgName);
        employeeMovementModal.selectJobName(targetJobName);
        employeeMovementModal.selectPositionName(targetPositionName);
        employeeMovementModal.selectMobility(isMobility);
        employeeMovementModal.selectMovementCategory(category);
        employeeMovementModal.setAsPJS(isPJS);
        employeeMovementModal.clickApplyButton();
    }

    public void removeIncumbent(String orgName, String positionName, String employeeNameOrId) {
        logger.info("Phase: --- Removing Incumbent: {} from Org: {} Position: {} ---", employeeNameOrId, orgName, positionName);
        assignEmployeePage.clickManageIncumbentOfNode(orgName);
        manageIncumbentModal.expandPositionAccordion(positionName);
        manageIncumbentModal.clickIncumbentKebabMenu(employeeNameOrId);
        manageIncumbentModal.selectKebabAction("Remove");
    }

    public void confirmAction(ConfirmAction action) {
        logger.info("Step: Confirming action: {}", action.getActionName());
        confirmationActionModal.clickYesButton();
        successModal.clickContinueButton();
    }

    @Deprecated
    public void confirmAction(String actionName) {
        for (ConfirmAction action : ConfirmAction.values()) {
            if (action.getActionName().equalsIgnoreCase(actionName)) {
                confirmAction(action);
                return;
            }
        }
        throw new IllegalArgumentException("Unknown confirm action: " + actionName);
    }

    public boolean verifyEmployeeAssignmentLog(String actionTitle, String... dynamicKeywords) {
        logger.info("Step: Verifying Employee Assignment Changes Log for Action [{}] with Keywords {}", actionTitle, dynamicKeywords);
        changesLogModal.openChangesLogModal();
        changesLogModal.selectEmployeeAssignmentTab();
        return changesLogModal.isOrganizationChangeLogDisplayed(actionTitle, dynamicKeywords);
    }
}
