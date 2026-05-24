package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.List;

public class AddJobPositionModal extends BaseFormModal {
    public AddJobPositionModal(WebDriver driver) {
        super(driver, "Add Job & Position");
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'Organization Level')]]//input")
    private WebElement getOrganizationLevel;

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'New Organization Name')]]//input")
    private WebElement getNewOrganizationName;

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'Parent Organization Name')]]//input")
    private WebElement getParentOrgName;

    @FindBy(xpath = "//div[./label[contains(normalize-space(), 'RC Code / Cost Center')]]//input")
    private WebElement getRCCode;

    private final By addButton = By.xpath("//div[@role='dialog']//button[normalize-space(text())='Add']");

// =========================
    public List<JobSectionComponent> getJobSections() {

        List<WebElement> sections = driver.findElements(
                By.xpath("//span[contains(.,'Job & Position')]/ancestor::div[contains(@class,'border rounded-lg')]")
        );

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
        click(driver.findElement(By.xpath("//button[normalize-space()='+ Add Job']")));
    }

    public void clickAddButton(){
        click(addButton);
    }

}
