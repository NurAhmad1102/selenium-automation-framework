package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.example.utils.actionUtils.ActionUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class SuccessModal extends BasePage {
    ActionUtils actionUtils;

    public SuccessModal(WebDriver driver) {
        super(driver);
        actionUtils = new ActionUtils(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@role='dialog']//button[normalize-space()='Continue']")
    private WebElement continueButton;

    public void clickContinueButton(){
        click(continueButton);
    }

    public boolean isSuccessModalHeaderDisplayed(String action){
        String formatInput = actionUtils.capitalizeFirstLetter(action);

        String xpath = String.format(
                "//div[@role='dialog']//h2[normalize-space()='Organization %s']",
                formatInput
        );

        return isVisible(By.xpath(xpath));
    }

    public boolean isSuccessModalDescriptionDisplayed(String description){
        String xpath = String.format(
                "//div[@role='dialog']//p[normalize-space()='%s']",
                description
        );

        return isVisible(By.xpath(xpath));
    }

    public boolean isSuccessModalImageDisplayed() {
        String xpath = "//div[@role='dialog']//img[contains(@src,'Confetti.png')]";
        return isVisible(By.xpath(xpath));
    }




}
