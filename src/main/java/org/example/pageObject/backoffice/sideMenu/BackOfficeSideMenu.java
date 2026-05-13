package org.example.pageObject.backoffice.sideMenu;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class BackOfficeSideMenu extends BasePage {

    public BackOfficeSideMenu(WebDriver driver){
        super(driver);
        PageFactory.initElements(driver, this);
    }

//    MANAGE ORGANIZATION
    @FindBy(xpath = "//div[text()=\"Manage Organization\"]")
    private WebElement manageOrganizationMenu;

    @FindBy(xpath = "//div[text()=\"SLA Setting\"]")
    private WebElement slaSettingSubMenu;

    public void clickManageOrganizationMenu(){
        click(manageOrganizationMenu);
    }

    public void clickSlaSettingSubMenu(){
        click(slaSettingSubMenu);
    }



//    WORKFLOW ENGINE
    @FindBy(xpath = "//div[text()=\"Workflow Engine\"]")
    private WebElement workflowEngineMenu;

    @FindBy(xpath = "//div[text()=\"Approval Matrix\"]")
    private WebElement approvalMatrixMenu;

    public void clickWorkflowEngineMenu(){
        click(workflowEngineMenu);
    }

    public void clickApprovalEngine(){
        click(approvalMatrixMenu);
    }

}
