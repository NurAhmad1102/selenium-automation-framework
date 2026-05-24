package org.example.pageObject.frontoffice.manageOrganization;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class DashboardManageOrganizationPage extends BasePage {

    public DashboardManageOrganizationPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//h2[normalize-space()='Manage Organization']")
    private WebElement manageOrganizationHeader;

    @FindBy(xpath = "//button[normalize-space()='Update Organization' or normalize-space()='Update']")
    private WebElement updateOrganizationButton;

    public boolean isManageOrganizationHeaderDisplayed(){
        return isVisible(manageOrganizationHeader);
    }

    public void clickUpdateOrganizationButton() {
        click(updateOrganizationButton);
    }
}
