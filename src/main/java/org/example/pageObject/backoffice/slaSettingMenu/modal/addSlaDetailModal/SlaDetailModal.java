package org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class SlaDetailModal extends BasePage {
    public SlaDetailModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@class=\"ant-modal-title\" and text()=\"Add SLA Details\"]")
    private WebElement addSlaDetailHeader;

    @FindBy(xpath = "//div[@class=\"ant-modal-title\" and text()=\"Edit SLA Details\"]")
    private WebElement editSlaDetailHeader;

    @FindBy(xpath = "//input[@id=\"type\"]")
    private WebElement slaDetailType;

    @FindBy(xpath = "//input[@id='condition']/ancestor::div[contains(@class, 'ant-select')]//div[contains(@class, 'ant-select-selector')]")
    private WebElement slaDetailCondition;

    @FindBy(xpath = "//div[@title=\"Multiple Changes\" and .//div[text()=\"Multiple Changes\"]]")
    private WebElement multipleChanges;

    @FindBy(xpath = "//div[@title=\"Single Change\" and .//div[text()=\"Single Change\"]]")
    private WebElement singleChange;

    @FindBy(id = "sla")
    private WebElement slaDetailSlaDay;

    @FindBy(xpath = "//button[span[text()=\"Save\"]]")
    private WebElement saveButton;

    @FindBy(xpath = "//button[span[text()=\"Yes\"]]")
    private WebElement createYesButton;


    @FindBy(xpath = "//h2[text()=\"Success\"]")
    private WebElement headerPopUpSuccess;

//    @FindBy(xpath = "//button[span[text()=\"Done\"]]")
    @FindBy(xpath = "//span[text()='Done']/ancestor::button")
    private WebElement donePopUpButton;

    public void isAddSlaDetailHeader(){
        if (!isVisible(addSlaDetailHeader)) {
            throw new RuntimeException("Add SLA Detail header not visible");
        }
    }

    public void isEditSlaDetailHeader(){
        if (!isVisible(editSlaDetailHeader)) {
            throw new RuntimeException("Edit SLA Detail header not visible");
        }
    }

    public WebElement setSlaDetailType(){
        return slaDetailType;
    }

    public void clickSlaDetailCondition(){
        click(slaDetailCondition);
    }

    public void clickMultipleChanges(){
        click(multipleChanges);
    }

//    public WebElement getMultipleChanges(){
//        return multipleChanges;
//    }

    public void clickSingleChange(){
        click(singleChange);
    }

    public WebElement setSlaDetailSlaDay(){
        return slaDetailSlaDay;
    }

    public void clickSaveButton(){
        click(saveButton);
    }


    //Alert Confirmation Create Sla Setting
    public void clickCreateYesButton(){
        click(createYesButton);
    }

    public boolean isHeaderPopUpSuccess(){
        return isVisible(headerPopUpSuccess);
    }

    public void clickDonePopUpButton(){
//        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
//        WebElement ele = wait.until(ExpectedConditions.visibilityOf(donePopUpButton));
        click(donePopUpButton);
    }


}




