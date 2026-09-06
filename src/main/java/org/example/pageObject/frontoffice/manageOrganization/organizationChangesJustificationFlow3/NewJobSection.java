package org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class NewJobSection extends BasePage {

    public NewJobSection(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//button[contains(., 'New Job')]")
    private WebElement newJobAccordionHeader;

    public boolean isNewJobSectionDisplayed() {
        scrollMoveToElement(newJobAccordionHeader);
        return isVisible(newJobAccordionHeader);
    }

    public void expandNewJobSection() {
        if (isVisible(newJobAccordionHeader)) {
            String state = newJobAccordionHeader.getAttribute("data-state");
            if ("closed".equalsIgnoreCase(state)) {
                click(newJobAccordionHeader);
            }
        }
    }

    public boolean isNewJobAndOrganizationDisplayed(String newJobName, String organizationName) {
        expandNewJobSection();
        String xpath = String.format(
                "//div[.//button[normalize-space(.)='New Job']]" +
                        "//div[.//*[normalize-space(text())='%s']]" +
                        "[.//*[normalize-space(text())='%s']]",
                newJobName,
                organizationName
        );

        return isVisible(By.xpath(xpath));
    }

}
