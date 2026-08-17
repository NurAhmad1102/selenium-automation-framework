package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.BaseFormModal;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

public class EditJobsPositionsModal extends BaseFormModal {

    public EditJobsPositionsModal(WebDriver driver) {
        super(driver, "Edit Jobs & Positions");
        PageFactory.initElements(driver, this);
    }

    private final By addJobSectionButton = By.xpath("//button[normalize-space()='+ Add Job']");
    private final By cancelButton = By.xpath("//div[@role='dialog']//button[normalize-space()='Cancel']");
    private final By saveButton = By.xpath("//div[@role='dialog']//button[normalize-space()='Save']");

    public List<JobSectionComponent> getJobSections() {
        List<WebElement> activeTabPanels = driver.findElements(By.xpath("//div[@role='tabpanel' and @data-state='active']"));

        String baseContext = !activeTabPanels.isEmpty()
                ? "//div[@role='tabpanel' and @data-state='active']"
                : "//div[@role='dialog']";

        String xpath = baseContext + "//*[self::span or self::button][contains(normalize-space(.),'Job & Position')]" +
                       "/ancestor::div[contains(@class,'border') and contains(@class,'rounded')][1]";

        List<WebElement> sections = driver.findElements(By.xpath(xpath));

        List<JobSectionComponent> jobs = new ArrayList<>();

        for (int i = 0; i < sections.size(); i++) {
            jobs.add(new JobSectionComponent(driver, sections.get(i), i));
        }

        return jobs;
    }

    public JobSectionComponent getJobSection(int index) {
        return getJobSections().get(index);
    }

    public void clickAddJobButton() {
        click(addJobSectionButton);
    }

    @Override
    public void clickCancelButton() {
        click(cancelButton);
    }

    public void clickSaveButton() {
        click(saveButton);
    }
}
