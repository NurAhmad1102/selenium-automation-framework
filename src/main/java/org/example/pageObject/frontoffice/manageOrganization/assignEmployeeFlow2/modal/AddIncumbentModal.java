package org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public class AddIncumbentModal extends BasePage {

    public AddIncumbentModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@role='dialog']//h2[normalize-space()='Add Incumbent']")
    private WebElement modalTitle;

    @FindBy(xpath = "//div[label[contains(., 'Incumbent Name')]]//button[@role='combobox']")
    private WebElement incumbentNameDropdown;

    @FindBy(xpath = "//button[@id='yes'] | //label[contains(text(), 'Yes, This movement')]")
    private WebElement mobilityYesRadio;

    @FindBy(xpath = "//button[@id='no'] | //label[contains(text(), 'No, This movement')]")
    private WebElement mobilityNoRadio;

    @FindBy(xpath = "//div[label[contains(., 'Movement Category')]]//button[@role='combobox']")
    private WebElement movementCategoryDropdown;

    @FindBy(xpath = "//button[@id='primaryFlag'] | //label[@for='primaryFlag']")
    private WebElement pjsCheckbox;

    @FindBy(xpath = "//div[@role='dialog']//button[normalize-space()='Cancel']")
    private WebElement cancelButton;

    @FindBy(xpath = "//div[@role='dialog']//button[normalize-space()='Apply']")
    private WebElement applyButton;

    private By continueButtonModal = By.xpath("//div[@role='dialog' and .//h2[normalize-space()='Incumbent Added']]//button[normalize-space()='Continue']\n");
    private By yesButtonModal = By.xpath("//div[@role='dialog' and .//h2[normalize-space()='Add Incumbent']]//button[normalize-space()='Yes']\n");

    public boolean isModalDisplayed() {
        return isVisible(modalTitle);
    }

    public void selectIncumbentName(String incumbentName) {
        selectDropdownModal(incumbentNameDropdown, incumbentName);
    }

    public void clickIncumbentNameDropdown() {
        click(incumbentNameDropdown);
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
        By pjsBy = By.xpath("//button[@id='primaryFlag']");
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

    public void clickYesButton() {
        click(yesButtonModal);
    }

    public void clickContinueButton() {
        click(continueButtonModal);
    }

    public boolean isNoDataFoundLovDisplayed() {
        By elementText = By.xpath("//div[@role='listbox']//*[normalize-space(.)='No data found'] | //*[normalize-space(.)='No data found' and not(*)]");
        return isPresent(elementText);
    }

    public boolean isNoDataFoundDisplayed() {
        return isNoDataFoundLovDisplayed();
    }

    public boolean hasAvailableIncumbents() {
        By optionsLocator = By.xpath("//div[@role='listbox']//div[@role='option'] | //div[@role='option']");
        List<WebElement> options = driver.findElements(optionsLocator);
        for (WebElement opt : options) {
            try {
                if (opt.isDisplayed() && !opt.getText().trim().equalsIgnoreCase("No data found")) {
                    return true;
                }
            } catch (Exception ignored) {}
        }
        return false;
    }

    public void selectFirstAvailableIncumbent() {
        By firstOption = By.xpath("//div[@role='listbox']//div[@role='option'][1] | //div[@role='option'][1]");
        click(firstOption);
    }
}
