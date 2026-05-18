package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;

public class SetOrganizationHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final SetOrganizationPage setOrganizationPage;
    
    // Default values from original helper
    private final String sub_directorate = "1022 Audit - Enablers ID";

    public SetOrganizationHelper(FrontOfficeMainPagesFactory mainPagesFactory) {
        this.setOrganizationPage = mainPagesFactory.manageOrganization()
                .getSetOrganizationFlow1();
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

    public void addNewOrganization(String newOrganizationName) {
        logger.info("Step: Adding New Organization: {}", newOrganizationName);
        clickKebabButtonOfNode(sub_directorate, OrgMenuAction.ADD_NEW);
        // Additional logic for filling the form can be added here
    }
}
