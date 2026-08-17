package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.BaseFormModal;
import org.openqa.selenium.By;
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

    private final By applyButton = By.xpath("//button[text()='Apply']");

    public boolean isMergeFormHeaderVisible(){
        return isModalHeaderDisplayed();
    }

    public void clickMergeOrgWithDropdown(String mergeOrgWith){
        logger.info("Step: Click Merge Org With Dropdown and select: {}", mergeOrgWith);
        selectDropdownModal(mergeOrgWithDropdown, mergeOrgWith);
    }

    public void clickApplyButton(){
        click(applyButton);
    }
}
