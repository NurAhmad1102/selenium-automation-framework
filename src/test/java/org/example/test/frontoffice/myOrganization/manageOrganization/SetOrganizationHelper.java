package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.*;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.Assert;

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
    private OrganizationTestData data;

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
        initializeDefaultData();
    }

    private void initializeDefaultData() {
        data = new OrganizationTestData();
        data.setPrimaryOrganization("O223 Network Sales Digitalization D2 ID");
        data.setMergeOrganizationWith("O223 Network Specialist D2 ID");
        data.setNewOrganizationName("New Organization");
        data.setRcCode("1611.00007.0401");

        data.setJobIndex(0);
        data.setPositionIndex(0);
        data.setJobName("Head of Advisory & Legal");
        data.setPositionNamingType("Same as Job");
        data.setLocation("Kantor Pusat - Menara Asia");
        data.setParentPosition("Network Sales Digitalization Head O223");
        data.setFteNumbers("2");
        data.setDottedMatrix(false);
    }

    public OrganizationTestData getTestData() {
        return data;
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

        if (action == OrgMenuAction.ADD_NEW || action == OrgMenuAction.MERGE || action == OrgMenuAction.SPLIT) {
            logger.info("Step: Setting FTE to [{}]", data.getFteNumbers());
            positionSection.setFTE(data.getFteNumbers());
        }

        if (action == OrgMenuAction.ADD_NEW) {
            logger.info("Step: Setting Dotted Matrix to [{}]", data.isDottedMatrix());
            positionSection.selectDottedMatrix(data.isDottedMatrix());
        }

        clickButtonProceedJobPosition(action);
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
    public void confirmAction(String actionName) {
        logger.info("Step: Confirming action: {}", actionName);
        Assert.assertTrue(confirmationActionModal.isConfirmationModalActionDisplayed(actionName),
                "Confirmation Action modal header is not displayed for action: " + actionName);
        confirmationActionModal.clickYesButton();
        String actionMessage = validateAction(actionName);
        verifySuccessAndContinue(actionMessage);
    }

    private void verifySuccessAndContinue(String successAction) {
        logger.info("Step: Verifying success modal for action: {}", successAction);
        Assert.assertTrue(successModal.isSuccessModalHeaderDisplayed(successAction),
                "Success modal header is not displayed for action: " + successAction);
        Assert.assertTrue(successModal.isSuccessModalDescriptionDisplayed(successAction),
                "Success modal description is not correct for action: " + successAction);
        Assert.assertTrue(successModal.isSuccessModalImageDisplayed(), "Confetti image is not displayed");
        successModal.clickContinueButton();
    }

    private String validateAction(String actionName) {
        logger.info("Step: Validating action: {}", actionName);
        String message = "";
        if(actionName.equals("Add")){
            message = "Added";
        }
        else if(actionName.equals("Merge")){
            message = "Merged";
        }
        else if(actionName.equals("Rename")){
            message = "Renamed";
        }
        else if(actionName.equals("Split")){
            message = "Split";
        }
        else if(actionName.equals("Move")){
            message = "Moved";
        }
        else if(actionName.equals("Remove")){
            message = "Removed";
        }
        return message;
    }

}
