package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class BaseFormModal extends BasePage {
    protected String formTitle;

    public BaseFormModal(WebDriver driver, String formTitle) {
        super(driver);
        PageFactory.initElements(driver, this);
        this.formTitle = formTitle;
    }

//    TODO CEK WEBELEMENT APA MASIH PERLU AATAU PAKAI MEHTOD HELPER ISVISIVLE
    private final By newOrgNameField = By.xpath("//label[contains(normalize-space(.), 'New Organization Name')]/following-sibling::input");
    private final By selectRCCodeDropdown = By.xpath("//button[@role='combobox']");

    public String setOrgNewName(String newName) {
        logger.info("Step: Setting New Organization Name to: {}", newName);
        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(newOrgNameField));
        typeText(element, newName);
        return newName;
    }

    protected By getModalContainer(){
        return By.xpath(String.format("//div[@role='dialog' and .//h2[text()='%s']]", formTitle));
    }

    public void clickCancelButton(){
        String xpath = String.format("//div[@role='dialog' and .//h2[text()='%s']]//button[text()='Cancel']", formTitle);
        click(By.xpath(xpath));
    }

    public void clickNext(){
        String xpath = String.format("//div[@role='dialog' and .//h2[text()='%s']]//button[text()='Next']", formTitle);
        click(By.xpath(xpath));
    }

    public void clickRCCodeDropdown(String value){
        logger.info("Step: Click RC Code Dropdown and select: {}", value);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(selectRCCodeDropdown));
        selectDropdownModal(element, value);
    }

    public boolean isModalHeaderDisplayed() {
        try {
            String xpath = String.format("//h2[normalize-space()='%s']", this.formTitle);
            return isVisible(By.xpath(xpath));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isInlineErrorMessageDisplayed(String expectedMessage) {
        try {
            String xpath = String.format( "//p[normalize-space(text())='%s']", expectedMessage);
            return isVisible(By.xpath(xpath));
        } catch (Exception e) {
            return false;
        }
    }


}
