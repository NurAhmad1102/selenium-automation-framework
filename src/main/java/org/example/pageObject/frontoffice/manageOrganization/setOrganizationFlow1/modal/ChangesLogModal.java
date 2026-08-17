package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class ChangesLogModal extends BasePage {

    public ChangesLogModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//button[normalize-space()='More'] | //button[contains(span/text(), 'More')]")
    private WebElement moreButton;

    @FindBy(xpath = "//*[@role='menuitem' and contains(., 'View Changes Log')]")
    private WebElement viewChangesLogMenuItem;

    @FindBy(xpath = "//div[@role='dialog']//h2[normalize-space()='Changes Log'] | //div[contains(@class,'font-bold') and text()='Changes Log']")
    private WebElement modalTitle;

    @FindBy(xpath = "//button[@role='tab' and contains(., 'Organization Changes')]")
    private WebElement organizationChangesTab;

    @FindBy(xpath = "//button[@role='tab' and contains(., 'Employee Assignment')]")
    private WebElement employeeAssignmentTab;

    public void openChangesLogModal() {
        click(moreButton);
        click(viewChangesLogMenuItem);
    }

    public void selectOrganizationChangesTab() {
        click(organizationChangesTab);
    }

    public void selectEmployeeAssignmentTab() {
        click(employeeAssignmentTab);
    }

    public boolean isModalDisplayed() {
        return isVisible(modalTitle);
    }

    /**
     * Memeriksa apakah log entry untuk aksi yang bersangkutan tampil di dalam modal,
     * dengan mencocokkan Judul Aksi dan kata kunci nama-nama organisasi secara dinamis.
     */
    public boolean isOrganizationChangeLogDisplayed(String actionTitle, String... dynamicOrgKeywords) {
        String containerXpath = "//div[@role='tabpanel' and not(@hidden)]";
        
        StringBuilder logXpath = new StringBuilder(containerXpath)
                .append("//div[contains(@class,'items-start') and .//div[contains(@class,'font-bold') and normalize-space()='")
                .append(actionTitle)
                .append("']]");

        for (String keyword : dynamicOrgKeywords) {
            if (keyword != null && !keyword.trim().isEmpty()) {
                logXpath.append("[.//div[contains(., \"").append(keyword).append("\")]]");
            }
        }

        By logBy = By.xpath(logXpath.toString());
        boolean isDisplayed = isVisible(logBy);

        logger.info("Validasi Log Isolated -> Action: [{}], Keywords: {}, Result: [{}]",
                actionTitle, dynamicOrgKeywords, isDisplayed);

        return isDisplayed;
    }
}
