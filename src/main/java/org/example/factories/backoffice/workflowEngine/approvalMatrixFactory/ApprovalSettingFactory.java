package org.example.factories.backoffice.workflowEngine.approvalMatrixFactory;

import org.example.pageObject.backoffice.workflowEngineMenu.approvalMatrixMenu.ApprovalMatrixPage;
import org.openqa.selenium.WebDriver;

public class ApprovalSettingFactory {
    private WebDriver driver;
    private ApprovalMatrixPage approvalMatrixPage;

    public ApprovalSettingFactory(WebDriver driver) {
        this.driver = driver;
    }

    public ApprovalMatrixPage getApprovalMatrixPage() {
        if(approvalMatrixPage == null){
            approvalMatrixPage = new ApprovalMatrixPage(driver);
        }
        return approvalMatrixPage;
    }

}
