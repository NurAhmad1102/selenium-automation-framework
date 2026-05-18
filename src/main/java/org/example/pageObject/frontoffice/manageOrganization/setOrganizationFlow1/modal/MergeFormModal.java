package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class MergeFormModal extends BaseFormModal {

    public MergeFormModal(WebDriver driver) {
        super(driver,"Merge Organization");
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'Organization Name')]]//button[@role='combobox']")
    private WebElement mergeOrgWithDropdown;

    public boolean isMergeFormHeaderVisible(){
        return isModalHeaderDisplayed();
    }

    public void clickMergeOrgWithDropdown(String mergeOrgWith){
        logger.info("Step: Click Merge Org With Dropdown and select: {}", mergeOrgWith);
        selectDropdownModal(mergeOrgWithDropdown, mergeOrgWith);
    }
}
