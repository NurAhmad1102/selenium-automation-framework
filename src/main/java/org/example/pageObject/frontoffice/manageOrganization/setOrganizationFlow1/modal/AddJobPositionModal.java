package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AddJobPositionModal extends BaseFormModal {
    public AddJobPositionModal(WebDriver driver) {
        super(driver, "Add Job & Position");
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'Organization Level')]]//input")
    private WebElement getOrganizationLevel;

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'New Organization Name')]]//input")
    private WebElement getNewOrganizationName;

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'Parent Organization Name')]]//input")
    private WebElement getParentOrgName;

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'RC Code / Cost Center')]]//input")
    private WebElement getRCCode;





}
