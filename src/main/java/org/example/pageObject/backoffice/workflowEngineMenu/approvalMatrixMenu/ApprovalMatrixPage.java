package org.example.pageObject.backoffice.workflowEngineMenu.approvalMatrixMenu;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ApprovalMatrixPage extends BasePage {

    public ApprovalMatrixPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

//    @FindBy

}
