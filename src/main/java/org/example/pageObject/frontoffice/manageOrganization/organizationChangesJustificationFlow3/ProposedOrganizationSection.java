package org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ProposedOrganizationSection extends BasePage {

    public ProposedOrganizationSection(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[contains(., 'Proposed Organization Structure') or contains(text(), 'Proposed Organization Structure')]")
    private WebElement proposedOrgSectionHeader;

    public boolean isProposedOrganizationSectionDisplayed() {
        scrollMoveToElement(proposedOrgSectionHeader);
        return isVisible(proposedOrgSectionHeader);
    }

    public boolean isOrgNodeDisplayedInProposedOrg(String orgName) {
        String xpath = String.format("//div[@id='proposed-org-chart']//*[text()='%s' or normalize-space()='%s']", orgName, orgName);
        return isVisible(By.xpath(xpath));
    }

    public boolean isOrgNodeInvisibleInProposedOrg(String orgName) {
        String xpath = String.format("//div[@id='proposed-org-chart']//*[text()='%s' or normalize-space()='%s']", orgName, orgName);
        return isNotVisible(By.xpath(xpath));
    }
}