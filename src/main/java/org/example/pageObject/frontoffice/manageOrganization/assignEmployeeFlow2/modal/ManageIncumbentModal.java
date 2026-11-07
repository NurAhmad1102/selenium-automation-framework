package org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ManageIncumbentModal extends BasePage {

    public ManageIncumbentModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@role='dialog']//h2[normalize-space()='Manage Incumbent']")
    private WebElement modalTitle;

    @FindBy(xpath = "//div[@role='dialog']//button[normalize-space()='Close']")
    private WebElement closeButton;

    public boolean isModalDisplayed() {
        return isVisible(modalTitle);
    }

    public void clickCloseButton() {
        if (isModalDisplayed()) {
            click(closeButton);
        }
    }

    public void expandPositionAccordion(String positionName) {
        String xpath = String.format("//button[contains(@id, 'radix-') and .//div[contains(text(), '%s')]]", positionName);
        By accordionBy = By.xpath(xpath);
        if (isVisible(accordionBy)) {
            WebElement accordionBtn = driver.findElement(accordionBy);
            String state = accordionBtn.getAttribute("data-state");
            if ("closed".equalsIgnoreCase(state)) {
                click(accordionBtn);
            }
        }
    }

    public String getPositionFteText(String positionName) {
        String xpath = String.format("//button[contains(@id, 'radix-') and .//div[contains(text(), '%s')]]//span[contains(text(), 'FTE')]", positionName);
        By fteBy = By.xpath(xpath);
        if (isVisible(fteBy)) {
            return driver.findElement(fteBy).getText().trim();
        }
        return "";
    }

    public void clickAddIncumbentButton(String positionName) {
        expandPositionAccordion(positionName);
        String xpath = String.format("//div[contains(@id, 'radix-') and .//div[contains(text(), '%s')]]//button[contains(., 'Add Incumbent')]", positionName);
        click(By.xpath(xpath));
    }

    public boolean isIncumbentDisplayed(String employeeNameOrId) {
        String xpath = String.format("//main[contains(@class, 'bg-neutral-100')]//section[.//h1[contains(., '%s')] or .//h2[contains(., '%s')]]", employeeNameOrId, employeeNameOrId);
        return isVisible(By.xpath(xpath));
    }

    public boolean isIncumbentNotDisplayed(String employeeNameOrId) {
        String xpath = String.format("//main[contains(@class, 'bg-neutral-100')]//section[.//h1[contains(., '%s')] or .//h2[contains(., '%s')]]", employeeNameOrId, employeeNameOrId);
        return isNotVisible(By.xpath(xpath));
    }

    public void clickIncumbentKebabMenu(String employeeNameOrId) {
        String xpath = String.format(
                "//main[contains(@class, 'bg-neutral-100')]//section[.//h1[contains(., '%s')] or .//h2[contains(., '%s')]]//button",
                employeeNameOrId,
                employeeNameOrId);

        click(By.xpath(xpath));
    }

    public void selectKebabAction(String actionName) {
        String xpath = String.format("//*[@role='menuitem' and normalize-space()='%s']", actionName);
        click(By.xpath(xpath));
    }

    public boolean isKebabActionDisplayed(String actionName) {
        String xpath = String.format("//*[@role='menuitem' and normalize-space()='%s']", actionName);
        return isVisible(By.xpath(xpath));
    }

    public boolean isAddIncumbentButtonDisabled(String positionName) {
        expandPositionAccordion(positionName);
        String xpath = String.format("//div[contains(@id, 'radix-') and .//div[contains(text(), '%s')]]//button[contains(., 'Add Incumbent')]", positionName);
        By btnBy = By.xpath(xpath);
        if (isVisible(btnBy)) {
            WebElement btn = driver.findElement(btnBy);
            return btn.getAttribute("disabled") != null || "true".equalsIgnoreCase(btn.getAttribute("aria-disabled"));
        }
        return false;
    }

    public boolean isTagDisplayed(String employeeNameOrId, String expectedTag) {
        String lowerTag = expectedTag.toLowerCase();
        String xpath = String.format(
                "//div[@role='dialog']//section[.//h1[contains(., '%1$s')] or .//h2[contains(., '%1$s')]]//span[translate(normalize-space(), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')='%2$s' or normalize-space()='%3$s']",
                employeeNameOrId,
                lowerTag,
                expectedTag
        );
        return isVisible(By.xpath(xpath));
    }

    public boolean isTagNewDisplayed(String employeeName) {
        return isTagDisplayed(employeeName, "New");
    }

    public boolean isTagPjsDisplayed(String employeeName) {
        return isTagDisplayed(employeeName, "PJS");
    }

    public boolean isTagMoveDisplayed(String employeeName) {
        return isTagDisplayed(employeeName, "Move");
    }
}