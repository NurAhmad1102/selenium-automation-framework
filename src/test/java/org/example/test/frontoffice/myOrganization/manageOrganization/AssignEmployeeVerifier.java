package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.factories.frontoffice.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.AssignEmployeePage;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.ManageIncumbentModal;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal.UnassignedEmployeeListModal;
import org.example.enums.frontoffice.manageOrganization.AssignmentTag;

public class AssignEmployeeVerifier {
    private final Logger logger = LogManager.getLogger(getClass());
    private final FrontOfficeMainPagesFactory mainPagesFactory;

    public AssignEmployeeVerifier(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.mainPagesFactory = mainPagesFactory;
    }

    private AssignEmployeePage assignEmployeePage() {
        return mainPagesFactory.manageOrganization().getAssignEmployeeFlow2Page();
    }

    private ManageIncumbentModal manageIncumbentModal() {
        return mainPagesFactory.manageOrganization().assignEmployeeModals().getManageIncumbentModal();
    }

    private UnassignedEmployeeListModal unassignedEmployeeListModal() {
        return mainPagesFactory.manageOrganization().assignEmployeeModals().getUnassignedEmployeeListModal();
    }

    public boolean isViewUnassignedButtonClickable() {
        return assignEmployeePage().isViewUnassignedButtonClickable();
    }

    public boolean verifyEmployeeInUnassignedListVisible(String employeeName) {
        logger.info("Step: Verifying if Employee [{}] is displayed in Unassigned Employee List", employeeName);
        assignEmployeePage().clickViewUnassignedButton();
        unassignedEmployeeListModal().searchUnassignedEmployee(employeeName);
        boolean isDisplayed = unassignedEmployeeListModal().isEmployeeDisplayed(employeeName);
        unassignedEmployeeListModal().clickCloseButton();
        return isDisplayed;
    }

    public boolean verifyEmployeeInUnassignedListNotVisible(String employeeName) {
        logger.info("Step: Verifying if Employee [{}] is NOT displayed in Unassigned Employee List", employeeName);
        assignEmployeePage().clickViewUnassignedButton();
        unassignedEmployeeListModal().searchUnassignedEmployee(employeeName);
        boolean isDisplayed = unassignedEmployeeListModal().isEmployeeNotDisplayed(employeeName);
        unassignedEmployeeListModal().clickCloseButton();
        return isDisplayed;
    }

    public boolean verifyEmployeeInPositionNotVisible(String orgName, String positionName, String employeeNameOrId) {
        logger.info("Step: Verifying if Employee [{}] is NOT displayed in Org: {} Position: {}", employeeNameOrId, orgName, positionName);
        assignEmployeePage().clickManageIncumbentOfNode(orgName);
        manageIncumbentModal().expandPositionAccordion(positionName);
        boolean isNotDisplayed = manageIncumbentModal().isIncumbentNotDisplayed(employeeNameOrId);
        manageIncumbentModal().clickCloseButton();
        return isNotDisplayed;
    }

    public boolean verifyEmployeeInPositionVisible(
            String orgName,
            String positionName,
            String employeeNameOrId,
            AssignmentTag expectedTag) {

        logger.info("Step: Verifying if Employee [{}] with Tag [{}] is displayed in Org: {} Position: {}",
                employeeNameOrId, expectedTag, orgName, positionName);
        assignEmployeePage().clickManageIncumbentOfNode(orgName);
        manageIncumbentModal().expandPositionAccordion(positionName);

        boolean isEmployeeDisplayed = manageIncumbentModal().isIncumbentDisplayed(employeeNameOrId);
        if (!isEmployeeDisplayed) {
            logger.error("[FAILED] Employee [{}] is NOT displayed in Org: [{}] Position: [{}]", employeeNameOrId, orgName, positionName);
            manageIncumbentModal().clickCloseButton();
            return false;
        }

        logger.info("[PASSED] Employee [{}] is displayed in Org: [{}] Position: [{}]", employeeNameOrId, orgName, positionName);

        boolean allTagsDisplayed = true;
        if (expectedTag != null && expectedTag != AssignmentTag.NONE) {
            for (String tag : expectedTag.getTags()) {
                boolean isTagDisplayed = manageIncumbentModal().isTagDisplayed(employeeNameOrId, tag);
                if (!isTagDisplayed) {
                    logger.error("[FAILED] Tag [{}] is NOT displayed for Employee [{}] in Org: [{}] Position: [{}]", tag, employeeNameOrId, orgName, positionName);
                    allTagsDisplayed = false;
                } else {
                    logger.info("[PASSED] Tag [{}] is displayed for Employee [{}]", tag, employeeNameOrId);
                }
            }
        }

        manageIncumbentModal().clickCloseButton();
        return isEmployeeDisplayed && allTagsDisplayed;
    }
}