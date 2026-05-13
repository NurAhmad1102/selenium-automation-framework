package org.example.test.backoffice.workflowEngine.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.factories.backoffice.login.BackOfficeLoginPagesFactory;
import org.example.factories.backoffice.sideMenu.SideMenuFactory;
import org.example.factories.backoffice.workflowEngine.WorkflowEngineFactory;
import org.example.pageObject.backoffice.login.BackOfficeLoginPage;
import org.example.utils.TestDataFactory;

public class Helper {
    private final Logger logger = LogManager.getLogger(getClass());
    BackOfficeLoginPage backOfficeLoginPage;
    SideMenuFactory sideMenuFactory;
    WorkflowEngineFactory workflowEngine;


    public Helper(
            BackOfficeLoginPagesFactory loginPages,
            SideMenuFactory sideMenuFactory,
            WorkflowEngineFactory workflowEngine
    ){
        backOfficeLoginPage = loginPages.getLoginPage();
        this.sideMenuFactory = sideMenuFactory;
        this.workflowEngine = workflowEngine;
    }

    private void goToWorkflowEngine(String nip){
       logger.info("Start login.....");
        String password = TestDataFactory.getPassword();

        backOfficeLoginPage.setUsernameField(nip);
        backOfficeLoginPage.setPasswordField(password);
        backOfficeLoginPage.clickLoginButton();
        backOfficeLoginPage.isHeaderWelcomeVisible();

        sideMenuFactory.getSideMenu().clickWorkflowEngineMenu();
    }

    public void goToApprovalEngine(String nip){
        logger.info("Go to Approval Engine Menu....");
        goToWorkflowEngine(nip);
        sideMenuFactory.getSideMenu().clickApprovalEngine();
    }


}
