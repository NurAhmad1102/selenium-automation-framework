package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.AddFormModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.AddJobPositionModal;

public class SetOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final SetOrganizationPage setOrganizationPage;
    private final AddFormModal addFormModal;
    private final AddJobPositionModal addJobPositionModal;

    public SetOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.setOrganizationPage = mainPagesFactory.manageOrganization()
                .getSetOrganizationFlow1Page();
        this.addFormModal = mainPagesFactory.manageOrganization()
                .modals()
                .getAddFormModal();
        this.addJobPositionModal = mainPagesFactory.manageOrganization()
                .modals()
                .getAddJobPositionModal();
    }

    private void clickOrganizationNode(String nodeName) {
        logger.info("Step: Selecting Request Type: Click Organization Node: {}", nodeName);
        setOrganizationPage.clickNodeOrganizationFlow1(nodeName);
    }

    private void clickKebabButtonOfNode(String nodeName, OrgMenuAction action) {
        logger.info("Step: Clicking kebab button of node: {}", nodeName);
        setOrganizationPage.clickKebabButtonOfNodeOrganizationFlow1(nodeName);
        setOrganizationPage.selectMenuActionFlow1(action);
    }

//    PERFORM ACTION
    public void fillJobPosition(
            int indexJobPositionSection,
            int indexPositionSection,
            String jobName,
            String positionNamingType,
            String location,
            String parentPosition,
            String fteNumbers,
            boolean isDottedMatrix
            ){


        logger.info("Step: Filling Job Position in the form");
        // Implement the logic to fill job position in the form
        var jobSection = addJobPositionModal.getJobSection(indexJobPositionSection);
        logger.info("Step: Filling Job & Position {}", indexJobPositionSection);
        logger.info("Step: Select Job Name {}", jobName);
        jobSection.selectJobName(jobName);

        var positionSection = jobSection.getPosition(indexPositionSection);
        logger.info("Step: Select Naming Type {}", positionNamingType);
        positionSection.selectPositionNamingType(positionNamingType);

        positionSection.selectLocation(location);
        positionSection.selectParentPositionName(parentPosition);
        positionSection.setFTE(fteNumbers);
        positionSection.selectDottedMatrix(isDottedMatrix);
        addJobPositionModal.clickAddButton();
    }

//    ADD NEW ORG
    public void addNewOrganization(String parentOrganization ,String newOrganizationName, String rcCode) {
        logger.info("Step: Adding New Organization: {}", newOrganizationName);
        clickKebabButtonOfNode(parentOrganization, OrgMenuAction.ADD_NEW);
        addFormModal.setOrgNewName(newOrganizationName);
        addFormModal.clickRCCodeDropdown(rcCode);
        addFormModal.clickNext();
    }


}
