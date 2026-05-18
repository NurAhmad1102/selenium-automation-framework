package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class MoveFormModal extends BaseFormModal {
    public MoveFormModal(WebDriver driver) {
        super(driver, "Move Organization");
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'Organization Level')]]//button[@role='combobox']")
    private WebElement organizationLevelDropdown;

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'Parent Organization Name')]]//button[@role='combobox']")
    private WebElement newParentOrgNameDropdown;

    public boolean isMoveFormHeaderVisible(){
        return isModalHeaderDisplayed();
    }

    public void clickOrganizationLevelDropdown(String organizationLevel) {
        logger.info("Step: Click Organization Level Dropdown and select: {}", organizationLevel);
        selectDropdownModal(organizationLevelDropdown, organizationLevel);
    }

    public void clickNewParentOrgName(String newParentOrgName) {
        logger.info("Step: Click New Parent Organization Name Dropdown and select: {}", newParentOrgName);
        selectDropdownModal(newParentOrgNameDropdown, newParentOrgName);
    }






}
