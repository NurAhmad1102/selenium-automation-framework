package org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CurrentOrganizationSection extends BasePage {

    public CurrentOrganizationSection(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[contains(., 'Current Organization Structure') or contains(text(), 'Current Organization Structure')]")
    private WebElement currentOrgSectionHeader;

    public boolean isCurrentOrganizationSectionDisplayed() {
        scrollMoveToElement(currentOrgSectionHeader);
        return isVisible(currentOrgSectionHeader);
    }

    public boolean isOrgNodeDisplayedInCurrentOrg(String orgName) {
        String xpath = String.format("//div[@id='current-org-chart']//*[text()='%s' or normalize-space()='%s']", orgName, orgName);
        return isVisible(By.xpath(xpath));
    }

    public boolean isOrgNodeInvisibleInCurrentOrg(String orgName) {
        String xpath = String.format("//div[@id='current-org-chart']//*[text()='%s' or normalize-space()='%s']", orgName, orgName);
        return isNotVisible(By.xpath(xpath));
    }
}