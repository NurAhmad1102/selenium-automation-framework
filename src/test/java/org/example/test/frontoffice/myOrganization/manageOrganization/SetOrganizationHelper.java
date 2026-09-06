package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.factories.frontoffice.FrontOfficeMainPagesFactory;
import org.example.enums.frontoffice.manageOrganization.ConfirmAction;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ChangesLogModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ConfirmationActionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.NodeOrganizationModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.PreviewProposedOrg;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal.*;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.ManageOrganizationExcelReader;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.model.frontoffice.MassiveOrgModel;

public class SetOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final FrontOfficeMainPagesFactory mainPagesFactory;

    public SetOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.mainPagesFactory = mainPagesFactory;
    }

    // Lazy getters for Page and Modals
    private SetOrganizationPage setOrganizationPage() {
        return mainPagesFactory.manageOrganization().getSetOrganizationFlow1Page();
    }

    private AddFormModal addFormModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getAddFormModal();
    }

    private MergeFormModal mergeFormModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getMergeFormModal();
    }

    private RenameFormModal renameFormModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getRenameFormModal();
    }

    private MoveFormModal moveFormModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getMoveFormModal();
    }

    private SplitFormModal splitFormModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getSplitFormModal();
    }

    private AddJobPositionModal addJobPositionModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getAddJobPositionModal();
    }

    private ConfirmationActionModal confirmationActionModal() {
        return mainPagesFactory.manageOrganization().commonModals().getConfirmationActionModal();
    }

    private SuccessModal successModal() {
        return mainPagesFactory.manageOrganization().commonModals().getSuccessModal();
    }

    private ChangesLogModal changesLogModal() {
        return mainPagesFactory.manageOrganization().commonModals().getChangesLogModal();
    }

    public NodeOrganizationModal nodeOrganizationModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getNodeOrganizationModal();
    }

    public EditJobsPositionsModal editJobsPositionsModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getEditJobsPositionsModal();
    }

    public PreviewProposedOrg previewProposedOrg() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getPreviewProposedOrg();
    }

    public OrganizationTestData getTestData(OrgSheetName sheet) {
        return ManageOrganizationExcelReader.getSingleData(sheet);
    }

    public OrganizationTestData getTestData(OrgSheetName sheet, int rowIndex) {
        return ManageOrganizationExcelReader.getDataByRow(sheet, rowIndex);
    }

    public OrganizationTestData getTestData(OrgSheetName sheet, String actionName) {
        return ManageOrganizationExcelReader.getDataByAction(sheet, actionName);
    }

    private void clickOrganizationNode(String nodeName) {
        logger.info("Step: Selecting Request Type: Click Organization Node: {}", nodeName);
        setOrganizationPage().clickNodeOrganizationFlow1(nodeName);
    }

    private void clickKebabButtonOfNode(String nodeName, OrgMenuAction action) {
        logger.info("Action: Clicking kebab button of node [{}] with action [{}]", nodeName, action);
        setOrganizationPage().clickKebabButtonOfNodeOrganizationFlow1(nodeName);
        setOrganizationPage().selectMenuActionFlow1(action);
    }

    public boolean isNodeOrganizationDisplayed(String orgName) {
        logger.info("Step: Search Organization Name: Get Organization Node: {}", orgName);
        return setOrganizationPage().isNodeOrganizationDisplayed(orgName);
    }

    public boolean isNodeOrganizationInvisible(String orgName) {
        logger.info("Step: Search Organization Name: Get Organization Node: {}", orgName);
        return setOrganizationPage().isNodeOrganizationInvisible(orgName);
    }

    public boolean isInlineErrorMessageDisplayed(String orgName) {
        logger.info("Step: Validation inline error message: Get Inline Error Message: {}", orgName);
        return addFormModal().isInlineErrorMessageDisplayed(orgName);
    }

    // PERFORM ACTIONS
    public void fillJobPosition(OrganizationTestData data, OrgMenuAction action) {
        fillJobPosition(data, action, action == OrgMenuAction.ADD_NEW || action == OrgMenuAction.MERGE || action == OrgMenuAction.SPLIT, true);
    }

    public void fillJobPosition(OrganizationTestData data, OrgMenuAction action, boolean setFte, boolean proceed) {
        logger.info("Phase: --- Filling Job & Position Details ---");

        var jobSection = addJobPositionModal().getJobSection(data.getJobIndex());
        logger.info("Step: Working on Job Section index [{}]", data.getJobIndex());

        logger.info("Step: Selecting Job Name [{}]", data.getJobName());
        jobSection.selectJobName(data.getJobName());

        var positionSection = jobSection.getPosition(data.getPositionIndex());
        logger.info("Step: Working on Position Section index [{}]", data.getPositionIndex());

        logger.info("Step: Selecting Naming Type [{}]", data.getPositionNamingType());
        positionSection.selectPositionNamingType(data.getPositionNamingType());

        logger.info("Step: Selecting Location [{}]", data.getLocation());
        positionSection.selectLocation(data.getLocation());

        logger.info("Step: Selecting Parent Position [{}]", data.getParentPosition());
        positionSection.selectParentPositionName(data.getParentPosition());

        if (setFte) {
            logger.info("Step: Setting FTE to [{}]", data.getFteNumbers());
            positionSection.setFTE(data.getFteNumbers());
        }

        if (action == OrgMenuAction.ADD_NEW) {
            logger.info("Step: Setting Dotted Matrix to [{}]", data.isDottedMatrix());
            positionSection.selectDottedMatrix(data.isDottedMatrix());
        }

        if (proceed) {
            clickButtonProceedJobPosition(action);
        }
    }

    public void fillJobPositionWithNewJob(
            OrganizationTestData data,
            OrgMenuAction action,
            String customJobName,
            boolean setFte
    ) {
        logger.info("Phase: --- Filling Job & Position Details with New Job Creation: [{}] ---", customJobName);

        var jobSection = addJobPositionModal().getJobSection(data.getJobIndex());
        jobSection.createNewJobName(customJobName);

        var positionSection = jobSection.getPosition(data.getPositionIndex());
        positionSection.selectPositionNamingType(data.getPositionNamingType());
        positionSection.selectLocation(data.getLocation());
        positionSection.selectParentPositionName(data.getParentPosition());

        if (setFte) {
            logger.info("Step: Setting FTE to [{}]", data.getFteNumbers());
            positionSection.setFTE(data.getFteNumbers());
        }

        if (action == OrgMenuAction.ADD_NEW) {
            positionSection.selectDottedMatrix(data.isDottedMatrix());
        }

        clickButtonProceedJobPosition(action);
    }

    private void clickButtonProceedJobPosition(OrgMenuAction action) {
        if (action == OrgMenuAction.RENAME) {
            logger.info("Action: Clicking Apply button to finalize");
            addJobPositionModal().clickApplyButton();
        } else if (action == OrgMenuAction.MOVE) {
            logger.info("Action: Clicking Move button to finalize");
            addJobPositionModal().clickMoveButton();
        } else {
            logger.info("Action: Clicking Add button to finalize");
            addJobPositionModal().clickAddButton();
        }
    }

    public boolean verifyMissingParentPositionDisplayed() {
        logger.info("Step: Verify missing parent position displayed in the organization tree");
        return setOrganizationPage().isMissingParentPositionNodeDisplayed();
    }

    public void addNewOrganization(OrganizationTestData data) {
        logger.info("Phase: --- Adding New Organization: {}---", data.getNewOrganizationName());
        clickKebabButtonOfNode(data.getPrimaryOrganization(), OrgMenuAction.ADD_NEW);
        setOrganizationName(data, OrgMenuAction.ADD_NEW);
    }

    public void addMassiveOrganizationNode(MassiveOrgModel model) {
        logger.info("Phase: --- Adding Massive Org Node [{}] (Level {}) under Parent [{}] ---",
                model.getOrgName(), model.getLevel(), model.getParentOrgName());

        clickKebabButtonOfNode(model.getParentOrgName(), OrgMenuAction.ADD_NEW);

        logger.info("Step: Inputting Organization Name [{}] and RC Code [{}]", model.getOrgName(), model.getRcCode());
        addFormModal().setOrgNewName(model.getOrgName());
        addFormModal().clickRCCodeDropdown(model.getRcCode());
        addFormModal().clickNext();

        logger.info("Phase: --- Filling Job & Position for Massive Node [{}] ---", model.getOrgName());
        var jobSection = addJobPositionModal().getJobSection(0);
        logger.info("Step: Selecting Job Name [{}]", model.getJobName());
        jobSection.selectJobName(model.getJobName());

        var positionSection = jobSection.getPosition(0);
        logger.info("Step: Selecting Position Naming Type [{}]", model.getPositionNamingType());
        positionSection.selectPositionNamingType(model.getPositionNamingType());

        logger.info("Step: Selecting Location [{}]", model.getLocation());
        positionSection.selectLocation(model.getLocation());

        logger.info("Step: Selecting Parent Position Name [{}]", model.getParentPositionName());
        positionSection.selectParentPositionName(model.getParentPositionName());

        logger.info("Step: Setting FTE [{}] and Dotted Matrix [{}]", model.getFte(), model.isDottedMatrix());
        positionSection.setFTE(model.getFte());
        positionSection.selectDottedMatrix(model.isDottedMatrix());

        logger.info("Step: Typing Custom Position Name [{}] (Final Step before Add)", model.getPositionName());
        positionSection.setPositionName(model.getPositionName());

        logger.info("Action: Clicking Add button to finalize Job & Position");
        addJobPositionModal().clickAddButton();

        confirmAction(ConfirmAction.ADD);
    }

    public void removeOrganization(String organizationName) {
        logger.info("Phase: --- Remove Organization: {}---", organizationName);
        clickKebabButtonOfNode(organizationName, OrgMenuAction.REMOVE);
    }

    public void renameOrganization(OrganizationTestData data) {
        logger.info("Phase: --- Rename Organization: {}---", data.getPrimaryOrganization());
        clickKebabButtonOfNode(data.getPrimaryOrganization(), OrgMenuAction.RENAME);
        setOrganizationName(data, OrgMenuAction.RENAME);
    }

    public void mergeOrganization(OrganizationTestData data) {
        logger.info("Phase: --- Merge Organization: {}---", data.getPrimaryOrganization());
        clickKebabButtonOfNode(data.getPrimaryOrganization(), OrgMenuAction.MERGE);

        logger.info("Step: select Merge Organization with: {}", data.getMergeOrganizationWith());
        mergeFormModal().clickMergeOrgWithDropdown(data.getMergeOrganizationWith());
        logger.info("Step: Clicking Next to proceed to confirmation modal");
        mergeFormModal().clickNext();

        setOrganizationName(data, OrgMenuAction.MERGE);
    }

    public void moveOrganization(OrganizationTestData data) {
        logger.info("Phase: --- Move Organization: {}---", data.getPrimaryOrganization());
        clickKebabButtonOfNode(data.getPrimaryOrganization(), OrgMenuAction.MOVE);

        logger.info("Step: Selecting Organization Level [{}]", data.getLevel());
        moveFormModal().clickOrganizationLevelDropdown(data.getLevel());

        logger.info("Step: Inputting New Organization Name [{}]", data.getNewOrganizationName());
        moveFormModal().setOrgNewName(data.getNewOrganizationName());

        logger.info("Step: Selecting New Parent Organization [{}]", data.getNewParentOrganization());
        moveFormModal().clickNewParentOrgName(data.getNewParentOrganization());

        logger.info("Step: Selecting RC Code [{}]", data.getRcCode());
        moveFormModal().clickRCCodeDropdown(data.getRcCode());

        logger.info("Action: Clicking Next to proceed to Job Position modal");
        moveFormModal().clickNext();
    }

    public void splitOrganization(OrganizationTestData dataOrg1, OrganizationTestData dataOrg2) {
        logger.info("Phase: --- Split Organization: {} ---", dataOrg1.getPrimaryOrganization());
        clickKebabButtonOfNode(dataOrg1.getPrimaryOrganization(), OrgMenuAction.SPLIT);

        logger.info("Step: Filling Organization 1 Details");
        splitFormModal().fillOrganization1Details(dataOrg1.getNewOrganizationName1(), dataOrg1.getRcCode());

        logger.info("Step: Filling Organization 2 Details");
        splitFormModal().fillOrganization2Details(dataOrg2.getNewOrganizationName2(), dataOrg2.getRcCode());

        logger.info("Action: Clicking Next to proceed to Job & Position modal");
        splitFormModal().clickNext();
    }

    public void fillJobPositionForSplit(OrganizationTestData dataOrg1, OrganizationTestData dataOrg2) {
        fillJobPositionForSplit(dataOrg1, dataOrg2, true);
    }

    public void fillJobPositionForSplit(OrganizationTestData dataOrg1, OrganizationTestData dataOrg2, boolean proceed) {
        logger.info("Step: Filling Job & Position for Organization 1");
        addJobPositionModal().selectOrganizationTab("Organization 1");
        fillJobPosition(dataOrg1, OrgMenuAction.SPLIT, false, false);

        logger.info("Step: Filling Job & Position for Organization 2");
        addJobPositionModal().selectOrganizationTab("Organization 2");
        fillJobPosition(dataOrg2, OrgMenuAction.SPLIT, true, proceed);
    }

    private void setOrganizationName(OrganizationTestData data, OrgMenuAction orgMenuAction) {
        logger.info("Phase: --- Set Organization Name: {}---", data.getPrimaryOrganization());
        logger.info("Step: Inputting New Organization Name: {}", data.getNewOrganizationName());
        addFormModal().setOrgNewName(data.getNewOrganizationName());
        logger.info("Step: Selecting RC Code [{}]", data.getRcCode());
        addFormModal().clickRCCodeDropdown(data.getRcCode());
        logger.info("Action: Clicking Next to proceed to Job Position modal");

        if (orgMenuAction == OrgMenuAction.MERGE) {
            mergeFormModal().clickApplyButton();
        } else if (orgMenuAction == OrgMenuAction.ADD_NEW) {
            addFormModal().clickNext();
        } else if (orgMenuAction == OrgMenuAction.MOVE) {
            moveFormModal().clickNext();
        } else if (orgMenuAction == OrgMenuAction.RENAME) {
            renameFormModal().clickNext();
        }
    }

    // AFTER ACTIONS
    public void confirmAction(ConfirmAction action) {
        logger.info("Step: Confirming action: {}", action.getActionName());
        confirmationActionModal().clickYesButton();
        successModal().clickContinueButton();
    }

    public void verifyDetailNodeOrganization(OrganizationTestData orgData) {
        new SetOrganizationVerifier(mainPagesFactory).verifyDetailNodeOrganization(orgData);
    }

    public void clickChangeLogButton() {
        logger.info("Step: Clicking Changelog button");
        changesLogModal().openChangesLogModal();
        changesLogModal().selectOrganizationChangesTab();
    }

    public boolean verifyOrganizationChangesLog(String actionTitle, String... dynamicOrgKeywords) {
        return new SetOrganizationVerifier(mainPagesFactory).verifyOrganizationChangesLog(actionTitle, dynamicOrgKeywords);
    }

    public void setParentPositionToNode(String parentPositionName) {
        while (verifyMissingParentPositionDisplayed()) {
            setOrganizationPage().clickNodeMissingParentPosition();
            nodeOrganizationModal().clickEditButton();

            var jobSection = editJobsPositionsModal().getJobSection(0);
            var positionSection = jobSection.getPosition(0);

            logger.info("Step: Setting Child Node Parent Position to [{}]", parentPositionName);
            positionSection.selectParentPositionName(parentPositionName);

            editJobsPositionsModal().clickSaveButton();
            confirmationActionModal().clickYesButton();
            successModal().clickContinueButton();
        }
    }

    public void nextToAssignEmployee() {
        previewProposedOrg().isModalDisplayed();
        previewProposedOrg().clickNextButton();
    }
}