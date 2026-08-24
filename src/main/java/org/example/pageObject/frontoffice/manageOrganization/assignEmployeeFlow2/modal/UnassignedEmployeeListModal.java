package org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class UnassignedEmployeeListModal extends BasePage {

    public UnassignedEmployeeListModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@role='dialog']//h2[normalize-space()='Unassigned Employee List']")
    private WebElement modalTitle;

    @FindBy(css = "input[name='searchEmployee']")
    private WebElement searchInput;

    @FindBy(xpath = "//button[@role='tab' and contains(., 'Primary')]")
    private WebElement primaryTab;

    @FindBy(xpath = "//button[@role='tab' and contains(., 'Secondary')]")
    private WebElement secondaryTab;

    @FindBy(xpath = "//div[@role='dialog'][.//h2[normalize-space()='Unassigned Employee List']]//div[contains(@class,'ui-modal-header-theme')]/button")
    private WebElement closeButton;

    public boolean isModalDisplayed() {
        return isVisible(modalTitle);
    }

    public void clickCloseButton() {
        click(closeButton);
    }

    public void searchUnassignedEmployee(String employeeName) {
        typeText(searchInput, employeeName);
    }

    public void switchTab(String tabName) {
        if ("Primary".equalsIgnoreCase(tabName)) {
            click(primaryTab);
        } else if ("Secondary".equalsIgnoreCase(tabName)) {
            click(secondaryTab);
        }
    }

    public boolean isEmployeeDisplayed(String employeeNameOrId) {
        String xpath = String.format("//div[@role='tabpanel' and not(@hidden)]//div[contains(@class, 'rounded')]//h1[contains(., '%s')]", employeeNameOrId);
        return isVisible(By.xpath(xpath));
    }

    public String getFirstEmployeeName() {
        By firstEmployeeBy = By.xpath("//div[@role='tabpanel' and not(@hidden)]//div[contains(@class, 'rounded')]//h1");
        if (isVisible(firstEmployeeBy)) {
            return driver.findElement(firstEmployeeBy).getText().trim();
        }
        return "";
    }

    public void clickFirstMoveEmployeeButton() {
        By moveBtnBy = By.xpath("//div[@role='tabpanel' and not(@hidden)]//div[contains(@class, 'rounded')]//button[contains(., 'Move Employee')]");
        click(moveBtnBy);
    }

    public void clickMoveEmployee(String employeeNameOrId) {
        String xpath = String.format("//div[@role='tabpanel' and not(@hidden)]//div[contains(@class, 'rounded') and .//h1[contains(., '%s')]]//button[contains(., 'Move Employee')]", employeeNameOrId);
        click(By.xpath(xpath));
    }
}
