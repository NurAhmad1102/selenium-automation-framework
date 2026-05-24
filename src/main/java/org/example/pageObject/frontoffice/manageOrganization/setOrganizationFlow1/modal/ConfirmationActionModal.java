package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.example.utils.actionUtils.ActionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class ConfirmationActionModal extends BasePage{
    ActionUtils actionUtils;

    public ConfirmationActionModal(WebDriver driver) {
        super(driver);
        actionUtils = new ActionUtils(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@role='dialog' and .//h2[normalize-space()='Add Organization']]")
    private WebElement mainDialogContainer;

    @FindBy(xpath = "//button[normalize-space()='No']")
    private WebElement noButton;

    @FindBy(xpath = "//button[normalize-space()='Yes']")
    private WebElement yesButton;

    public boolean isConfirmationModalActionDisplayed(String action){
        String formatInput = actionUtils.capitalizeFirstLetter(action);
        String xpath = String.format("//div[@role='dialog']//h2[normalize-space()='" + formatInput + " Organization']");
        return isVisible(By.xpath(xpath));
    }

    public void clickYesButton(){
        click(yesButton);
    }

    public void clickNoButton(){
        click(noButton);
    }

}
