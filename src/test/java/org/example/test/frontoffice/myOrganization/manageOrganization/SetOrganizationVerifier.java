package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.factories.frontoffice.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ChangesLogModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.NodeOrganizationModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal.AddFormModal;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.example.utils.CustomSoftAssert;

public class SetOrganizationVerifier {
    private final Logger logger = LogManager.getLogger(getClass());
    private final FrontOfficeMainPagesFactory mainPagesFactory;

    public SetOrganizationVerifier(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.mainPagesFactory = mainPagesFactory;
    }

    private SetOrganizationPage setOrganizationPage() {
        return mainPagesFactory.manageOrganization().getSetOrganizationFlow1Page();
    }

    private AddFormModal addFormModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getAddFormModal();
    }

    private NodeOrganizationModal nodeOrganizationModal() {
        return mainPagesFactory.manageOrganization().setOrganizationModals().getNodeOrganizationModal();
    }

    private SuccessModal successModal() {
        return mainPagesFactory.manageOrganization().commonModals().getSuccessModal();
    }

    private ChangesLogModal changesLogModal() {
        return mainPagesFactory.manageOrganization().commonModals().getChangesLogModal();
    }

    public boolean isNodeOrganizationDisplayed(String orgName) {
        logger.info("Step: Search Organization Name: Get Organization Node: {}", orgName);
        return setOrganizationPage().isNodeOrganizationDisplayed(orgName);
    }

    public void verifyNodeOrganizationDisplayed(String orgName) {
        logger.info("Verification: Asserting node organization [{}] is displayed", orgName);
        CustomAssert.assertTrue(isNodeOrganizationDisplayed(orgName),
                "New Organization node is not displayed in the organization tree: " + orgName);
    }

    public boolean isNodeOrganizationInvisible(String orgName) {
        logger.info("Step: Search Organization Name: Verify Organization Node is invisible: {}", orgName);
        return setOrganizationPage().isNodeOrganizationInvisible(orgName);
    }

    public void verifyNodeOrganizationInvisible(String orgName) {
        logger.info("Verification: Asserting node organization [{}] is invisible", orgName);
        CustomAssert.assertTrue(isNodeOrganizationInvisible(orgName),
                "Organization node should not be displayed in the organization tree: " + orgName);
    }

    public boolean isInlineErrorMessageDisplayed(String errorMessage) {
        logger.info("Step: Validation inline error message: Get Inline Error Message: {}", errorMessage);
        return addFormModal().isInlineErrorMessageDisplayed(errorMessage);
    }

    public void verifyInlineErrorMessageDisplayed(String errorMessage) {
        logger.info("Verification: Asserting inline error message [{}] is displayed", errorMessage);
        CustomAssert.assertTrue(isInlineErrorMessageDisplayed(errorMessage),
                "Inline error message is not displayed: " + errorMessage);
    }

    public boolean verifyMissingParentPositionDisplayed() {
        logger.info("Step: Verify missing parent position displayed in the organization tree");
        return setOrganizationPage().isMissingParentPositionNodeDisplayed();
    }

    public void verifyDetailNodeOrganization(OrganizationTestData orgData) {
        logger.info("Step: Verifying detail node organization modal: {}", orgData.getNewOrganizationName());
        CustomSoftAssert softAssert = new CustomSoftAssert();
        String positionName = orgData.getJobName();

        softAssert.assertTrue(setOrganizationPage().isTagNewNodeOrgDisplayed(orgData.getNewOrganizationName()),
                "Organization doesn't have tag new");
        
        logger.info("Step: Selecting Request Type: Click Organization Node: {}", orgData.getNewOrganizationName());
        setOrganizationPage().clickNodeOrganizationFlow1(orgData.getNewOrganizationName());
        
        softAssert.assertEquals(nodeOrganizationModal().getRcCode(), orgData.getRcCode(), "RC Code is not same as expected");

        nodeOrganizationModal().clickTogglePositionAccordion(positionName);
        softAssert.assertEquals(nodeOrganizationModal().getJobNameOfPosition(positionName), orgData.getJobName(), "Job Name is not same as expected");
        softAssert.assertEquals(nodeOrganizationModal().getLocationOfPosition(positionName), orgData.getLocation(), "Location is not same as expected");
        softAssert.assertAll();
        nodeOrganizationModal().clickCloseButton();
    }

    public void verifySuccessAndContinue(String successAction) {
        logger.info("Step: Verifying success modal for action: {}", successAction);
        CustomAssert.assertTrue(successModal().isSuccessModalHeaderDisplayed(successAction),
                "Success modal header is not displayed for action: " + successAction);
        CustomAssert.assertTrue(successModal().isSuccessModalDescriptionDisplayed(successAction),
                "Success modal description is not correct for action: " + successAction);
        CustomAssert.assertTrue(successModal().isSuccessModalImageDisplayed(), "Confetti image is not displayed");
        successModal().clickContinueButton();
    }

    public boolean verifyOrganizationChangesLog(String actionTitle, String... dynamicOrgKeywords) {
        logger.info("Step: Verifying Changes Log for Action [{}] with Keywords {}", actionTitle, dynamicOrgKeywords);
        return changesLogModal().isOrganizationChangeLogDisplayed(actionTitle, dynamicOrgKeywords);
    }
}