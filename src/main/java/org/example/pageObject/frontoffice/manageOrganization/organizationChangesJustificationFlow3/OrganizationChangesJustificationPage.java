package org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class OrganizationChangesJustificationPage extends BasePage {

    public OrganizationChangesJustificationPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//h2[normalize-space()='Update Organization']")
    private WebElement updateOrganizationHeader;

    @FindBy(xpath = "//div[normalize-space()='Organization Changes Justification' or contains(text(), 'Organization Changes Justification')]")
    private WebElement justificationHeaderStep;

    @FindBy(xpath = "//button[@id='actual-datepicker' or contains(@id, 'datepicker')]")
    private WebElement actualEffectiveDatePicker;

    @FindBy(xpath = "//textarea[@name='background' or contains(@placeholder, 'background') or contains(@placeholder, 'justification') or contains(@placeholder, 'Input justification')]")
    private WebElement backgroundTextarea;

    @FindBy(xpath = "//textarea[@name='requestorNotes' or contains(@placeholder, 'Requestor Notes') or contains(@placeholder, 'requestor')]")
    private WebElement requestorNotesTextarea;

    @FindBy(xpath = "//button[span[text()='Save as Draft'] or contains(., 'Save as Draft')]")
    private WebElement saveAsDraftButton;

    @FindBy(xpath = "//button[span[text()='More'] or contains(., 'More')]")
    private WebElement moreButton;

    @FindBy(xpath = "//button[text()='Back' or normalize-space()='Back']")
    private WebElement backButton;

    @FindBy(xpath = "//button[text()='Submit' or normalize-space()='Submit']")
    private WebElement submitButton;

    public boolean isOrganizationChangesJustificationPageDisplayed() {
        return isVisible(updateOrganizationHeader) || isVisible(justificationHeaderStep) || isVisible(submitButton);
    }

    public void setBackgroundJustification(String text) {
        if (isVisible(backgroundTextarea)) {
            typeText(backgroundTextarea, text);
        }
    }

    public String getBackgroundJustificationText() {
        if (isVisible(backgroundTextarea)) {
            return backgroundTextarea.getAttribute("value");
        }
        return "";
    }

    public void setRequestorNotes(String text) {
        if (isVisible(requestorNotesTextarea)) {
            typeText(requestorNotesTextarea, text);
        }
    }

    public String getRequestorNotesText() {
        if (isVisible(requestorNotesTextarea)) {
            return requestorNotesTextarea.getAttribute("value");
        }
        return "";
    }

    public void clickActualEffectiveDate() {
        if (isVisible(actualEffectiveDatePicker)) {
            click(actualEffectiveDatePicker);
        }
    }

    public void clickSaveAsDraftButton() {
        click(saveAsDraftButton);
    }

    public void clickMoreButton() {
        click(moreButton);
    }

    public void clickBackButton() {
        click(backButton);
    }

    public void clickSubmitButton() {
        scrollMoveToElement(submitButton);
        click(submitButton);
    }

    public boolean isSubmitButtonDisplayed() {
        return isVisible(submitButton);
    }

    public boolean isSubmitButtonClickable() {
        return isClickable(submitButton);
    }

    public boolean isSaveAsDraftButtonDisplayed() {
        return isVisible(saveAsDraftButton);
    }

    public boolean isSectionAccordionDisplayed(String sectionName) {
        String xpath = String.format("//button[contains(., '%s')]", sectionName);
        return isVisible(By.xpath(xpath));
    }

    public void clickSectionAccordion(String sectionName) {
        String xpath = String.format("//button[contains(., '%s')]", sectionName);
        click(By.xpath(xpath));
    }
}