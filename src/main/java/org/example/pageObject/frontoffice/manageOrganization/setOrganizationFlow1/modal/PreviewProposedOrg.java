package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class PreviewProposedOrg extends BasePage {

    public PreviewProposedOrg(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    // --- Page Factory WebElements ---

    @FindBy(xpath = "//h2[normalize-space()='Preview Proposed Organization']")
    private WebElement modalTitleHeader;

    @FindBy(xpath = "//div[contains(@class,'ui-modal-header-theme')]//button[.//svg] | //h2[normalize-space()='Preview Proposed Organization']/following-sibling::button")
    private WebElement closeButton;

    @FindBy(xpath = "//span[text()='Sub Directorate']/following-sibling::span")
    private WebElement subDirectorateText;

    @FindBy(xpath = "//span[text()='Division']/following-sibling::span")
    private WebElement divisionText;

    @FindBy(xpath = "//span[text()='Effective Date']/following-sibling::span")
    private WebElement effectiveDateText;

    @FindBy(xpath = "//footer//button[normalize-space()='Cancel']")
    private WebElement cancelButton;

    @FindBy(xpath = "//footer//button[normalize-space()='Next']")
    private WebElement nextButton;

    @FindBy(xpath = "//footer//span[contains(text(),'%')]")
    private WebElement zoomPercentageText;

    // --- Action & Verification Methods ---

    public boolean isModalDisplayed() {
        return isVisible(modalTitleHeader);
    }

    public void clickCloseButton() {
        try {
            click(closeButton);
        } catch (Exception e) {
            logger.warn("Standard click on closeButton failed, executing JS click fallback.", e);
            By fallbackBy = By.xpath("//div[contains(@class,'ui-modal-header-theme')]//button[.//svg]");
            clickWithJS(fallbackBy);
        }
    }

    public String getSubDirectorate() {
        return getText(subDirectorateText);
    }

    public String getDivision() {
        return getText(divisionText);
    }

    public String getEffectiveDate() {
        return getText(effectiveDateText);
    }

    public boolean isOrgNodeDisplayed(String orgName) {
        By xpath = By.xpath(String.format("//div[contains(@class,'org-node-card')]//p[contains(@class,'org-node-title') and contains(text(),'%s')]", orgName));
        return isVisible(xpath);
    }

    public boolean isOrgNodeWithJobDisplayed(String orgName, String jobName) {
        By xpath = By.xpath(String.format(
                "//div[contains(@class,'org-node-card') and .//p[contains(@class,'org-node-title') and contains(text(),'%s')]]//p[contains(text(),'%s')]",
                orgName, jobName
        ));
        return isVisible(xpath);
    }

    public void clickCancelButton() {
        click(cancelButton);
    }

    public void clickNextButton() {
        click(nextButton);
    }

    public String getZoomPercentage() {
        return getText(zoomPercentageText);
    }
}
