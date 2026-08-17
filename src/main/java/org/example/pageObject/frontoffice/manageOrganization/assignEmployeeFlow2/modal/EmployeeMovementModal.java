package org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class EmployeeMovementModal extends BasePage {

    public EmployeeMovementModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@role='dialog']//h2[normalize-space()='Employee Movement']")
    private WebElement modalTitle;

    @FindBy(css = "input[name='employeeName']")
    private WebElement employeeNameInput;

    @FindBy(css = "input[name='from.orgLevel']")
    private WebElement fromOrgLevelInput;

    @FindBy(css = "input[name='from.departmentName']")
    private WebElement fromDepartmentNameInput;

    @FindBy(css = "input[name='from.jobName']")
    private WebElement fromJobNameInput;

    @FindBy(css = "input[name='from.positionName']")
    private WebElement fromPositionNameInput;

    @FindBy(xpath = "//div[label[contains(., 'Organization Level')]]//button[@role='combobox']")
    private WebElement toOrgLevelDropdown;

    @FindBy(xpath = "//div[label[contains(., 'Organization Name')]]//button[@role='combobox']")
    private WebElement toOrgNameDropdown;

    @FindBy(xpath = "//div[label[contains(., 'Job Name')]]//button[@role='combobox']")
    private WebElement toJobNameDropdown;

    @FindBy(xpath = "//div[label[contains(., 'Position Name')]]//button[@role='combobox']")
    private WebElement toPositionNameDropdown;

    @FindBy(xpath = "//button[@id='yes'] | //label[contains(text(), 'Yes, This movement')]")
    private WebElement mobilityYesRadio;

    @FindBy(xpath = "//button[@id='no'] | //label[contains(text(), 'No, This movement')]")
    private WebElement mobilityNoRadio;

    @FindBy(xpath = "//div[label[contains(., 'Movement Category')]]//button[@role='combobox']")
    private WebElement movementCategoryDropdown;

    @FindBy(xpath = "//button[@id='to.primaryFlag'] | //label[@for='to.primaryFlag']")
    private WebElement pjsCheckbox;

    @FindBy(xpath = "//div[@role='dialog']//button[normalize-space()='Cancel']")
    private WebElement cancelButton;

    @FindBy(xpath = "//div[@role='dialog']//button[normalize-space()='Apply']")
    private WebElement applyButton;

    public boolean isModalDisplayed() {
        return isVisible(modalTitle);
    }

    public String getEmployeeName() {
        return getAttributeValue(employeeNameInput, "value");
    }

    public String getFromOrgLevel() {
        return getAttributeValue(fromOrgLevelInput, "value");
    }

    public String getFromDepartmentName() {
        return getAttributeValue(fromDepartmentNameInput, "value");
    }

    public String getFromJobName() {
        return getAttributeValue(fromJobNameInput, "value");
    }

    public String getFromPositionName() {
        return getAttributeValue(fromPositionNameInput, "value");
    }

    public void selectOrganizationLevel(String level) {
        selectDropdownModal(toOrgLevelDropdown, level);
    }

    public void selectOrganizationName(String orgName) {
        selectDropdownModal(toOrgNameDropdown, orgName);
    }

    public void selectJobName(String jobName) {
        selectDropdownModal(toJobNameDropdown, jobName);
    }

    public void selectPositionName(String positionName) {
        selectDropdownModal(toPositionNameDropdown, positionName);
    }

    public void selectMobility(boolean isMobility) {
        if (isMobility) {
            click(mobilityYesRadio);
        } else {
            click(mobilityNoRadio);
        }
    }

    public void selectMovementCategory(String category) {
        selectDropdownModal(movementCategoryDropdown, category);
    }

    public void setAsPJS(boolean isPJS) {
        By pjsBy = By.xpath("//button[@id='to.primaryFlag']");
        if (isVisible(pjsBy)) {
            WebElement element = driver.findElement(pjsBy);
            String checked = element.getAttribute("data-state");
            boolean currentlyChecked = "checked".equalsIgnoreCase(checked);
            if (isPJS != currentlyChecked) {
                click(element);
            }
        } else {
            click(pjsCheckbox);
        }
    }

    public void clickCancelButton() {
        click(cancelButton);
    }

    public void clickApplyButton() {
        click(applyButton);
    }
}