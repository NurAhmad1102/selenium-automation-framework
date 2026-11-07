package org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ProposedAssignmentEmployeeSection extends BasePage {

    public ProposedAssignmentEmployeeSection(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//button[contains(., 'Proposed Employee Assignment')]")
    private WebElement proposedAssignmentAccordionHeader;

    public boolean isProposedAssignmentEmployeeSectionDisplayed() {
        scrollMoveToElement(proposedAssignmentAccordionHeader);
        return isVisible(proposedAssignmentAccordionHeader);
    }

    public void expandProposedAssignmentSection() {
        if (isVisible(proposedAssignmentAccordionHeader)) {
            String state = proposedAssignmentAccordionHeader.getAttribute("data-state");
            if ("closed".equalsIgnoreCase(state)) {
                click(proposedAssignmentAccordionHeader);
            }
        }
    }

    public boolean isEmployeeAssignmentDisplayed(String employeeIdentifier, String movementCategory) {
        expandProposedAssignmentSection();
        String xpath = String.format("//button[.//h3[contains(normalize-space(), '%s')] and .//p[contains(normalize-space(), '%s')]]", employeeIdentifier, movementCategory);
        return isVisible(By.xpath(xpath));
    }

    public void clickEmployeeItem(String employeeIdentifier, String movementCategory) {
        expandProposedAssignmentSection();
        String xpath = String.format("//button[.//h3[contains(normalize-space(), '%s')] and .//p[contains(normalize-space(), '%s')]]", employeeIdentifier, movementCategory);
        By locator = By.xpath(xpath);
        if (isVisible(locator)) {
            WebElement button = driver.findElement(locator);
            String state = button.getAttribute("data-state");
            if ("closed".equalsIgnoreCase(state)) {
                click(button);
            }
        }
    }

    public String getMovementCategory(String employeeIdentifier, String movementCategory) {
        expandProposedAssignmentSection();
        String xpath = String.format("//button[.//h3[contains(normalize-space(), '%s')] and .//p[contains(normalize-space(), '%s')]]//p[contains(text(), 'Movement Category')]", employeeIdentifier, movementCategory);
        if (isVisible(By.xpath(xpath))) {
            return getText(By.xpath(xpath));
        }
        return "";
    }

    public String getAssignmentDetailValue(String employeeIdentifier, String movementCategory, String rowLabel, String colType) {
        expandProposedAssignmentSection();
        clickEmployeeItem(employeeIdentifier, movementCategory);
        int colIdx = "Proposed".equalsIgnoreCase(colType) ? 3 : 2;
        String xpath = String.format("//div[button[.//h3[contains(normalize-space(), '%s')] and .//p[contains(normalize-space(), '%s')]]]//tr[td[contains(normalize-space(), '%s')]]/td[%d]", employeeIdentifier, movementCategory, rowLabel, colIdx);
        if (isVisible(By.xpath(xpath))) {
            return getText(By.xpath(xpath));
        }
        return "";
    }

    public boolean isAssignmentDetailCorrect(String employeeIdentifier, String movementCategory, String expectedProposedOrg, String expectedProposedPosition) {
        expandProposedAssignmentSection();
        if (!isEmployeeAssignmentDisplayed(employeeIdentifier, movementCategory)) {
            return false;
        }
        String proposedOrg = getAssignmentDetailValue(employeeIdentifier, movementCategory, "Organization Name", "Proposed");
        String proposedPosition = getAssignmentDetailValue(employeeIdentifier, movementCategory, "Position Name", "Proposed");
        return proposedOrg.contains(expectedProposedOrg) && proposedPosition.contains(expectedProposedPosition);
    }
}