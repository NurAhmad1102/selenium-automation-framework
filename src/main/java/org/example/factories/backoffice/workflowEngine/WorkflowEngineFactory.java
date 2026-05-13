package org.example.factories.backoffice.workflowEngine;

import org.example.factories.backoffice.workflowEngine.approvalMatrixFactory.ApprovalSettingFactory;
import org.openqa.selenium.WebDriver;

public class WorkflowEngineFactory {
    private WebDriver driver;
    private ApprovalSettingFactory approvalSettingFactory;

    public WorkflowEngineFactory(WebDriver driver) {
        this.driver = driver;
    }

    public ApprovalSettingFactory getApprovalSettingFactory() {
        if (approvalSettingFactory == null){
            approvalSettingFactory = new ApprovalSettingFactory(driver);
        }
        return approvalSettingFactory;
    }

}
