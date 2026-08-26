package org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class AssignEmployeePage extends BasePage {

    public AssignEmployeePage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//h2[normalize-space()='Update Organization']")
    private WebElement updateOrganizationHeader;

    @FindBy(xpath = "//button[span[text()='Save as Draft']]")
    private WebElement saveAsDraftButton;

    @FindBy(xpath = "//button[span[text()='Upload']]")
    private WebElement uploadButton;

    @FindBy(xpath = "//button[span[text()='More']]")
    private WebElement moreButton;

    @FindBy(xpath = "//h1[contains(., 'Unassigned Employee')]")
    private WebElement unassignedBannerTitle;

    @FindBy(xpath = "//h1[contains(., 'Unassigned Employee')]/ancestor::div[contains(@class, 'flex flex-col')]//button[contains(., 'View')]")
    private WebElement viewUnassignedButton;

    @FindBy(xpath = "//button[text()='Back']")
    private WebElement backButton;

    @FindBy(xpath = "//button[text()='Next']")
    private WebElement nextButton;

    public boolean isAssignEmployeePageDisplayed() {
        return isVisible(updateOrganizationHeader);
    }

    public void clickSaveAsDraftButton() {
        click(saveAsDraftButton);
    }

    public void clickUploadButton() {
        click(uploadButton);
    }

    public void clickMoreButton() {
        click(moreButton);
    }

    public String getUnassignedEmployeeCountText() {
        if (isVisible(unassignedBannerTitle)) {
            return unassignedBannerTitle.getText().trim();
        }
        return "0";
    }

    public void clickViewUnassignedButton() {
        click(viewUnassignedButton);
    }
    public boolean isViewUnassignedButtonClickable() {
        return isClickable(viewUnassignedButton);
    }

    public void clickBackButton() {
        click(backButton);
    }

    public void clickNextButton() {
        click(nextButton);
    }

    public void clickNodeOrganization(String organizationName) {
        String xpath = String.format("//h1[text()='%s']/ancestor::div[@data-orientation='vertical']", organizationName);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        click(element);
    }

    public void clickManageIncumbentOfNode(String organizationName) {
        String xpath = String.format("//h1[text()='%s']/ancestor::div[@data-orientation='vertical']//button[contains(., 'Manage Incumbent')]", organizationName);
        WebElement btn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        click(btn);
    }

    public boolean isNodeOrganizationDisplayed(String organizationName) {
        String xpath = String.format("//h1[text()='%s']", organizationName);
        return isVisible(By.xpath(xpath));
    }

    public boolean isNodeTagDisplayed(String organizationName, String tagText) {
        String xpath = String.format("//div[@data-orientation='vertical' and .//h1[text()='%s']]//span[normalize-space()='%s']", organizationName, tagText);
        return isVisible(By.xpath(xpath));
    }
}