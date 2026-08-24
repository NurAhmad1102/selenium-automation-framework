package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NodeOrganizationModal extends BasePage {

    public NodeOrganizationModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    // --- Page Factory WebElements ---

    @FindBy(xpath = "//h2[contains(@class,'ui-modal-title-theme')] | //div[@role='dialog']//h2")
    private WebElement organizationTitleHeader;

    @FindBy(xpath = "//span[contains(text(),'RC Code:')]")
    private WebElement rcCodeBadge;

    @FindBy(xpath = "//h2[contains(@class,'ui-modal-title-theme')]/parent::div/following-sibling::button | //div[@role='dialog']//div[contains(@class,'border-b')]//button[.//svg] | //div[contains(@class,'border-b')]//button[.//svg]")
    private WebElement closeButton;

    @FindBy(xpath = "//button[normalize-space()='Edit']")
    private WebElement editButton;

    @FindBy(xpath = "//*[contains(text(),'Missing parent position')]")
    private WebElement missingParentPositionBanner;

    // --- Action & Verification Methods ---

    public boolean isModalDisplayed() {
        return isVisible(organizationTitleHeader);
    }

    public String getOrganizationTitle() {
        return getText(organizationTitleHeader);
    }

    public boolean isOrganizationTitleDisplayed(String expectedTitle) {
        By xpath = By.xpath(String.format("//h2[contains(@class,'ui-modal-title-theme') and normalize-space()='%s']", expectedTitle));
        return isVisible(xpath);
    }

    public String getRcCode() {
        String fullText = getText(rcCodeBadge);
        if (fullText.contains("RC Code:")) {
            return fullText.replace("RC Code:", "").trim();
        }
        return fullText.trim();
    }

    public boolean isRcCodeDisplayed(String expectedRcCode) {
        By xpath = By.xpath(String.format("//span[contains(text(),'RC Code:') and contains(text(),'%s')]", expectedRcCode));
        return isVisible(xpath);
    }

    public void clickCloseButton() {
        click(closeButton);
    }

    public void clickEditButton() {
        click(editButton);
    }

    public boolean isMissingParentPositionDisplayed() {
        return isVisible(missingParentPositionBanner);
    }

    public boolean isPositionAccordionDisplayed(String positionName) {
        By xpath = By.xpath(String.format("//button[contains(@class,'group') and contains(.,'%s')]", positionName));
        return isVisible(xpath);
    }

    public void clickTogglePositionAccordion(String positionName) {
        String xpath = "//button[@type='button' " +
                "and @aria-expanded " +
                "and contains(normalize-space(.), \"" +
                positionName +
                "\")]";
        click(By.xpath(xpath));
    }

    public String getJobNameOfPosition(String positionName) {
        String xpath = String.format(
                "//div[contains(@class,'border') and .//button[contains(.,'%s')]]//p[preceding-sibling::p[contains(text(),'JOB')]]",
                positionName
        );
        return getText(By.xpath(xpath));
    }

    public String getLocationOfPosition(String positionName) {
        String xpath = String.format(
                "//div[contains(@class,'border') and .//button[contains(.,'%s')]]//p[preceding-sibling::p[contains(text(),'LOCATION')]]",
                positionName
        );
        return getText(By.xpath(xpath));
    }
}
