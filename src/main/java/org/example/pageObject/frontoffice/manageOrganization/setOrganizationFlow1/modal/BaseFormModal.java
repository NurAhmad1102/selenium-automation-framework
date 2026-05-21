package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class BaseFormModal extends BasePage {
    protected String formTitle;

    public BaseFormModal(WebDriver driver, String formTitle) {
        super(driver);
        PageFactory.initElements(driver, this);
        this.formTitle = formTitle;
    }

    @FindBy(xpath = "//label[contains(normalize-space(.), 'New Organization Name')]/following-sibling::input")
    private WebElement newOrgNameField;

    @FindBy(xpath = "//button[@role='combobox']")
    private WebElement selectRCCodeDropdown;

    public String setOrgNewName(String newName) {
        logger.info("Step: Setting New Organization Name to: {}", newName);
        typeText(newOrgNameField, newName);
        return newName;
    }

    protected By getModalContainer(){
        return By.xpath(String.format("//div[@role='dialog' and .//h2[text()='%s']]", formTitle));
    }

    public void clickCancelButton(){
        String xpath = String.format("//div[@role='dialog' and .//h2[text()='%s']]//button[text()='Cancel']", formTitle);
        click(driver.findElement(By.xpath(xpath)));
    }

    public void clickNext(){
        String xpath = String.format("//div[@role='dialog' and .//h2[text()='%s']]//button[text()='Next']", formTitle);
        click(driver.findElement(By.xpath(xpath)));
    }

    public void clickRCCodeDropdown(String value){
        logger.info("Step: Click RC Code Dropdown and select: {}", value);
        selectDropdownModal(selectRCCodeDropdown,value);
    }

    public boolean isModalHeaderDisplayed() {
        try {
            String xpath = String.format("//h2[normalize-space()='%s']", this.formTitle);
            return isVisible(By.xpath(xpath));
        } catch (Exception e) {
            return false;
        }
    }



}
