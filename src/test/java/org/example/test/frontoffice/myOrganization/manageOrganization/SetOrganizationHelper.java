package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.*;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.model.OrganizationTestData;
import org.testng.Assert;

public class SetOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final SetOrganizationPage setOrganizationPage;
    private final AddFormModal addFormModal;
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
        data.setParentOrganization("O223 Network Sales Digitalization D2 ID");
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
    public void fillJobPosition(OrganizationTestData data) {
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

        logger.info("Step: Setting FTE to [{}]", data.getFteNumbers());
        positionSection.setFTE(data.getFteNumbers());

        logger.info("Step: Setting Dotted Matrix to [{}]", data.isDottedMatrix());
        positionSection.selectDottedMatrix(data.isDottedMatrix());

        logger.info("Action: Clicking Add button to finalize");
        addJobPositionModal.clickAddButton();
    }

//    ADD NEW ORG
    public void addNewOrganization(OrganizationTestData data) {
        logger.info("Phase: --- Adding New Organization: {}---", data.getNewOrganizationName());
        clickKebabButtonOfNode(data.getParentOrganization(), OrgMenuAction.ADD_NEW);

        logger.info("Step: Inputting New Organization Name: {}", data.getNewOrganizationName());
        addFormModal.setOrgNewName(data.getNewOrganizationName());

        logger.info("Step: Selecting RC Code [{}]", data.getRcCode());
        addFormModal.clickRCCodeDropdown(data.getRcCode());

        logger.info("Action: Clicking Next to proceed to Job Position modal");
        addFormModal.clickNext();
    }

//    REMOVE ORGANIZATION
    public void removeOrganization(String organizationName) {
        logger.info("Phase: --- Remove Organization: {}---", organizationName);
        clickKebabButtonOfNode(organizationName, OrgMenuAction.REMOVE);
    }


//    AFTER ACTIONS
    public void confirmAction(String actionName) {
        logger.info("Step: Confirming action: {}", actionName);
        Assert.assertTrue(confirmationActionModal.isConfirmationModalActionDisplayed(actionName),
                "Confirmation Action modal header is not displayed for action: " + actionName);
        confirmationActionModal.clickYesButton();
    }

    public void verifySuccessAndContinue(String successAction) {
        logger.info("Step: Verifying success modal for action: {}", successAction);
        Assert.assertTrue(successModal.isSuccessModalHeaderDisplayed(successAction),
                "Success modal header is not displayed for action: " + successAction);
        Assert.assertTrue(successModal.isSuccessModalDescriptionDisplayed(successAction),
                "Success modal description is not correct for action: " + successAction);
        Assert.assertTrue(successModal.isSuccessModalImageDisplayed(), "Confetti image is not displayed");
        successModal.clickContinueButton();
    }


}
