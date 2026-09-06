package org.example.factories.backoffice;

import org.example.factories.backoffice.login.BackOfficeLoginPagesFactory;
import org.example.factories.backoffice.manageOrganization.ManageOrganizationFactory;
import org.example.factories.backoffice.sideMenu.SideMenuFactory;
import org.example.factories.backoffice.workflowEngine.WorkflowEngineFactory;
import org.openqa.selenium.WebDriver;

public class BackOfficeMainPagesFactory {
    private final WebDriver driver;

    private BackOfficeLoginPagesFactory loginPagesFactory;
    private ManageOrganizationFactory manageOrganizationFactory;
    private SideMenuFactory sideMenuFactory;
    private WorkflowEngineFactory workflowEngineFactory;

    public BackOfficeMainPagesFactory(WebDriver driver) {
        this.driver = driver;
    }



    public BackOfficeLoginPagesFactory login() {
        if (loginPagesFactory == null) {
            loginPagesFactory = new BackOfficeLoginPagesFactory(driver);
        }
        return loginPagesFactory;
    }

    public SideMenuFactory sideMenu() {
        if (sideMenuFactory == null) {
            sideMenuFactory = new SideMenuFactory(driver);
        }
        return sideMenuFactory;
    }

    public ManageOrganizationFactory manageOrganization() {
        if (manageOrganizationFactory == null) {
            manageOrganizationFactory = new ManageOrganizationFactory(driver);
        }
        return manageOrganizationFactory;
    }

    public WorkflowEngineFactory workflowEngine() {
        if (workflowEngineFactory == null) {
            workflowEngineFactory = new WorkflowEngineFactory(driver);
        }
        return workflowEngineFactory;
    }



}
