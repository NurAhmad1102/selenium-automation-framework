package org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewPositionSection extends BasePage {

    public NewPositionSection(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//button[contains(., 'New Position')]")
    private WebElement newPositionAccordionHeader;

    public boolean isNewPositionSectionDisplayed() {
        scrollMoveToElement(newPositionAccordionHeader);
        return isVisible(newPositionAccordionHeader);
    }

    public void expandNewPositionSection() {
        if (isVisible(newPositionAccordionHeader)) {
            String state = newPositionAccordionHeader.getAttribute("data-state");
            if ("closed".equalsIgnoreCase(state)) {
                click(newPositionAccordionHeader);
            }
        }
    }

    public boolean isPositionDisplayed(String positionName) {
        expandNewPositionSection();
        String xpath = String.format("//div[contains(@class, 'border')]//h3[contains(normalize-space(), '%s')]", positionName);
        return isVisible(By.xpath(xpath));
    }

    public void clickPositionItem(String positionName) {
        expandNewPositionSection();
        String xpath = String.format("//div[contains(@class, 'border')]//button[.//h3[contains(normalize-space(), '%s')]]", positionName);
        if (isVisible(By.xpath(xpath))) {
            click(By.xpath(xpath));
        }
    }

    public String getPositionParentInfo(String positionName) {
        expandNewPositionSection();
        String xpath = String.format("//div[contains(@class, 'border') and .//h3[contains(normalize-space(), '%s')]]//p[contains(text(), 'Parent Position')]", positionName);
        if (isVisible(By.xpath(xpath))) {
            return getText(By.xpath(xpath));
        }
        return "";
    }

    public String getPositionDetailValue(String positionName, String detailLabel) {
        expandNewPositionSection();
        clickPositionItem(positionName);
        String xpath = String.format("//div[contains(@class, 'border') and .//h3[contains(normalize-space(), '%s')]]//p[text()='%s']/following-sibling::p", positionName, detailLabel);
        if (isVisible(By.xpath(xpath))) {
            return getText(By.xpath(xpath));
        }
        return "";
    }

    public boolean isPositionDetailsCorrect(String positionName, String expectedOrgName, String expectedJobName) {
        expandNewPositionSection();
        if (!isPositionDisplayed(positionName)) {
            return false;
        }
        String actualOrgName = getPositionDetailValue(positionName, "Organization Name");
        String actualJobName = getPositionDetailValue(positionName, "Job Name");
        return actualOrgName.contains(expectedOrgName) && actualJobName.contains(expectedJobName);
    }
}