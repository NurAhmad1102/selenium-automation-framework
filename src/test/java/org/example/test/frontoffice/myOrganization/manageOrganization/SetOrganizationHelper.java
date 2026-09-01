package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ChangesLogModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ConfirmationActionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.NodeOrganizationModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.PreviewProposedOrg;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal.*;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.ManageOrganizationExcelReader;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.example.utils.CustomSoftAssert;

public class SetOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final SetOrganizationPage setOrganizationPage;

    private final AddFormModal addFormModal;
    private final MergeFormModal mergeFormModal;
    private final RenameFormModal renameFormModal;
    private final MoveFormModal moveFormModal;
    private final SplitFormModal splitFormModal;
    private final AddJobPositionModal addJobPositionModal;
    private final ConfirmationActionModal confirmationActionModal;
    private final SuccessModal successModal;
    private final ChangesLogModal changesLogModal;
    private final NodeOrganizationModal nodeOrganizationModal;
    private final EditJobsPositionsModal editJobsPositionsModal;
    private final PreviewProposedOrg previewProposedOrg;

    public SetOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.setOrganizationPage = mainPagesFactory.manageOrganization()
                .getSetOrganizationFlow1Page();
        this.addFormModal = mainPagesFactory.manageOrganization()
                .modals()
                .getAddFormModal();
        this.mergeFormModal = mainPagesFactory.manageOrganization()
                .modals()
                .getMergeFormModal();
        this.renameFormModal = mainPagesFactory.manageOrganization()
                .modals()
                .getRenameFormModal();
        this.moveFormModal = mainPagesFactory.manageOrganization()
                .modals()
                .getMoveFormModal();
        this.splitFormModal = mainPagesFactory.manageOrganization()
                .modals()
                .getSplitFormModal();

        this.addJobPositionModal = mainPagesFactory.manageOrganization()
                .modals()
                .getAddJobPositionModal();
        this.confirmationActionModal = mainPagesFactory.manageOrganization()
                .modals()
                .getConfirmationActionModal();
        this.successModal = mainPagesFactory.manageOrganization()
                .modals()
                .getSuccessModal();
        this.changesLogModal = mainPagesFactory.manageOrganization()
                .modals()
                .getChangesLogModal();
        this.nodeOrganizationModal = mainPagesFactory.manageOrganization()
                .modals()
                .getNodeOrganizationModal();
        this.editJobsPositionsModal = mainPagesFactory.manageOrganization()
                .modals()
                .getEditJobsPositionsModal();
        this.previewProposedOrg = mainPagesFactory.manageOrganization()
                .modals()
                .getPreviewProposedOrg();
    }

    public NodeOrganizationModal nodeOrganizationModal() {
        return nodeOrganizationModal;
    }

    public EditJobsPositionsModal editJobsPositionsModal() {
        return editJobsPositionsModal;
    }

    public PreviewProposedOrg previewProposedOrg() {
        return previewProposedOrg;
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
        setOrganizationPage.clickNodeOrganizationFlow1(nodeName);
    }

    private void clickKebabButtonOfNode(String nodeName, OrgMenuAction action) {
        logger.info("Action: Clicking kebab button of node [{}] with action [{}]", nodeName, action);
        setOrganizationPage.clickKebabButtonOfNodeOrganizationFlow1(nodeName);
        setOrganizationPage.selectMenuActionFlow1(action);
    }

    public boolean isNodeOrganizationDisplayed(String orgName) {
        logger.info("Step: Search Organization Name: Get Organization Node: {}", orgName);
        return setOrganizationPage.isNodeOrganizationDisplayed(orgName);
    }

    public boolean isNodeOrganizationInvisible(String orgName) {
        logger.info("Step: Search Organization Name: Get Organization Node: {}", orgName);
        return setOrganizationPage.isNodeOrganizationInvisible(orgName);
    }

    public boolean isInlineErrorMessageDisplayed(String orgName) {
        logger.info("Step: Validation inline error message: Get Inline Error Message: {}", orgName);
        return addFormModal.isInlineErrorMessageDisplayed(orgName);
    }

//    PERFORM ACTION
    public void fillJobPosition(OrganizationTestData data, OrgMenuAction action) {
        fillJobPosition(data, action, action == OrgMenuAction.ADD_NEW || action == OrgMenuAction.MERGE || action == OrgMenuAction.SPLIT, true);
    }

//    public void fillJobPosition(OrganizationTestData data, OrgMenuAction action, boolean setFte) {
//        fillJobPosition(data, action, setFte, true);
//    }

    public void fillJobPosition(OrganizationTestData data, OrgMenuAction action, boolean setFte, boolean proceed) {

        if (action == OrgMenuAction.RENAME){
            CustomAssert.assertTrue(renameFormModal.isRenameJobPositionVisible(), "Rename Job Position modal header is not displayed after click next to modal Job & Position details");
        } else {
            CustomAssert.assertTrue(addJobPositionModal.isModalHeaderDisplayed(), "Add Job Position modal header is not displayed after click next to modal Job & Position details");
        }

        logger.info("Phase: --- Filling Job & Position Details ---");

        var jobSection = addJobPositionModal.getJobSection(data.getJobIndex());
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

    private void clickButtonProceedJobPosition(OrgMenuAction action) {
        if (action == OrgMenuAction.RENAME) {
            logger.info("Action: Clicking Apply button to finalize");
            addJobPositionModal.clickApplyButton();
        }
        else if (action == OrgMenuAction.MOVE) {
            logger.info("Action: Clicking Move button to finalize");
            addJobPositionModal.clickMoveButton();
        } else {
            logger.info("Action: Clicking Add button to finalize");
            addJobPositionModal.clickAddButton();
        }
    }

    public boolean verifyMissingParentPositionDisplayed() {
        logger.info("Step: Verify missing parent position displayed in the organization tree");
        return setOrganizationPage.isMissingParentPositionNodeDisplayed();
    }


//    ADD NEW ORG
    public void addNewOrganization(OrganizationTestData data) {
        logger.info("Phase: --- Adding New Organization: {}---", data.getNewOrganizationName());
        clickKebabButtonOfNode(data.getPrimaryOrganization(), OrgMenuAction.ADD_NEW);
        setOrganizationName(data, OrgMenuAction.ADD_NEW);
    }

//    REMOVE ORGANIZATION
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
        mergeFormModal.clickMergeOrgWithDropdown(data.getMergeOrganizationWith());
        logger.info("Step: Clicking Next to proceed to confirmation modal");
        mergeFormModal.clickNext();

        setOrganizationName(data, OrgMenuAction.MERGE);
    }

    public void moveOrganization(OrganizationTestData data) {
        logger.info("Phase: --- Move Organization: {}---", data.getPrimaryOrganization());
        clickKebabButtonOfNode(data.getPrimaryOrganization(), OrgMenuAction.MOVE);

        logger.info("Step: Selecting Organization Level [{}]", data.getLevel());
        moveFormModal.clickOrganizationLevelDropdown(data.getLevel());

        logger.info("Step: Inputting New Organization Name [{}]", data.getNewOrganizationName());
        moveFormModal.setOrgNewName(data.getNewOrganizationName());

        logger.info("Step: Selecting New Parent Organization [{}]", data.getNewParentOrganization());
        moveFormModal.clickNewParentOrgName(data.getNewParentOrganization());

        logger.info("Step: Selecting RC Code [{}]", data.getRcCode());
        moveFormModal.clickRCCodeDropdown(data.getRcCode());

        logger.info("Action: Clicking Next to proceed to Job Position modal");
        moveFormModal.clickNext();
    }

    public void splitOrganization(OrganizationTestData dataOrg1, OrganizationTestData dataOrg2) {
        logger.info("Phase: --- Split Organization: {} ---", dataOrg1.getPrimaryOrganization());
        clickKebabButtonOfNode(dataOrg1.getPrimaryOrganization(), OrgMenuAction.SPLIT);

        logger.info("Step: Filling Organization 1 Details");
        splitFormModal.fillOrganization1Details(dataOrg1.getNewOrganizationName1(), dataOrg1.getRcCode());

        logger.info("Step: Filling Organization 2 Details");
        splitFormModal.fillOrganization2Details(dataOrg2.getNewOrganizationName2(), dataOrg2.getRcCode());

        logger.info("Action: Clicking Next to proceed to Job & Position modal");
        splitFormModal.clickNext();
    }

    public void fillJobPositionForSplit(OrganizationTestData dataOrg1, OrganizationTestData dataOrg2) {
        fillJobPositionForSplit(dataOrg1, dataOrg2, true);
    }

    public void fillJobPositionForSplit(OrganizationTestData dataOrg1, OrganizationTestData dataOrg2, boolean proceed) {
        logger.info("Step: Filling Job & Position for Organization 1");
        addJobPositionModal.selectOrganizationTab("Organization 1");
        fillJobPosition(dataOrg1, OrgMenuAction.SPLIT, false, false);

        logger.info("Step: Filling Job & Position for Organization 2");
        addJobPositionModal.selectOrganizationTab("Organization 2");
        fillJobPosition(dataOrg2, OrgMenuAction.SPLIT, true, proceed);
    }

    private void setOrganizationName(OrganizationTestData data, OrgMenuAction orgMenuAction) {
        logger.info("Phase: --- Set Organization Name: {}---", data.getPrimaryOrganization());
        logger.info("Step: Inputting New Organization Name: {}", data.getNewOrganizationName());
        addFormModal.setOrgNewName(data.getNewOrganizationName());
        logger.info("Step: Selecting RC Code [{}]", data.getRcCode());
        addFormModal.clickRCCodeDropdown(data.getRcCode());
        logger.info("Action: Clicking Next to proceed to Job Position modal");

        if (orgMenuAction == OrgMenuAction.MERGE) {
            mergeFormModal.clickApplyButton();
        } else if (orgMenuAction == OrgMenuAction.ADD_NEW){
            addFormModal.clickNext();
        } else if (orgMenuAction == OrgMenuAction.MOVE){
            moveFormModal.clickNext();
        } else if (orgMenuAction == OrgMenuAction.RENAME){
            renameFormModal.clickNext();
        }

    }


//    AFTER ACTIONS
    public void confirmAction(ConfirmAction action) {
        logger.info("Step: Confirming action: {}", action.getActionName());
        CustomAssert.assertTrue(confirmationActionModal.isConfirmationModalActionDisplayed(action.getActionName()),
                "Confirmation Action modal header is not displayed for action: " + action.getActionName());
        confirmationActionModal.clickYesButton();
        verifySuccessAndContinue(action.getSuccessPastTense());
    }

    public void verifyDetailNodeOrganization(OrganizationTestData orgData){
        logger.info("Step: Verifying detail node organization modal: {}", orgData.getNewOrganizationName());
        CustomSoftAssert softAssert = new CustomSoftAssert();
        String positionName = orgData.getJobName();

        softAssert.assertTrue(setOrganizationPage.isTagNewNodeOrgDisplayed(orgData.getNewOrganizationName()),
                "Organization doesn't have tag new");
        clickOrganizationNode(orgData.getNewOrganizationName());
        softAssert.assertEquals(nodeOrganizationModal.getRcCode(), orgData.getRcCode(), "RC Code is not same as expected");

        nodeOrganizationModal.clickTogglePositionAccordion(positionName);
        softAssert.assertEquals(nodeOrganizationModal.getJobNameOfPosition(positionName), orgData.getJobName(), "Job Name is not same as expected");
        softAssert.assertEquals(nodeOrganizationModal.getLocationOfPosition(positionName), orgData.getLocation(), "Location is not same as expected");
        softAssert.assertAll();
        nodeOrganizationModal.clickCloseButton();
    }

    private void verifySuccessAndContinue(String successAction) {
        logger.info("Step: Verifying success modal for action: {}", successAction);
        CustomAssert.assertTrue(successModal.isSuccessModalHeaderDisplayed(successAction),
                "Success modal header is not displayed for action: " + successAction);
        CustomAssert.assertTrue(successModal.isSuccessModalDescriptionDisplayed(successAction),
                "Success modal description is not correct for action: " + successAction);
        CustomAssert.assertTrue(successModal.isSuccessModalImageDisplayed(), "Confetti image is not displayed");
        successModal.clickContinueButton();
    }

    public void clickChangeLogButton() {
        logger.info("Step: Clicking Changelog button");
        changesLogModal.openChangesLogModal();
        changesLogModal.selectOrganizationChangesTab();
    }

    public boolean verifyOrganizationChangesLog(String actionTitle, String... dynamicOrgKeywords) {
        logger.info("Step: Verifying Changes Log for Action [{}] with Keywords {}", actionTitle, dynamicOrgKeywords);
        return changesLogModal.isOrganizationChangeLogDisplayed(actionTitle, dynamicOrgKeywords);
    }

    /**
     * Resolves 'Missing parent position' on child organization nodes by selecting the specified parent position name.
     *
     * @param parentPositionName ke position yang missing parent position
     */
    public void setParentPositionToNode(String parentPositionName) {
        while (verifyMissingParentPositionDisplayed()){
            setOrganizationPage.clickNodeMissingParentPosition();
            nodeOrganizationModal.clickEditButton();

            // get Job & Position section di Child org paling pertama (default index 0)
            var jobSection = editJobsPositionsModal.getJobSection(0);
            var positionSection = jobSection.getPosition(0);

            logger.info("Step: Setting Child Node Parent Position to [{}]", parentPositionName);
            positionSection.selectParentPositionName(parentPositionName);

            editJobsPositionsModal.clickSaveButton();
            confirmationActionModal.clickYesButton();
            successModal.clickContinueButton();
//            nodeOrganizationModal.clickCloseButton();
        }
    }

    public void nextToAssignEmployee(){
        previewProposedOrg.isModalDisplayed();
        previewProposedOrg.clickNextButton();
    }

}
