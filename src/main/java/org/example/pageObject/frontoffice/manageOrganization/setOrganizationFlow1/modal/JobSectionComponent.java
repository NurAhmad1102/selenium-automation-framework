package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;

public class JobSectionComponent extends BasePage {
    private final WebElement root;

    public JobSectionComponent(WebDriver driver, WebElement root) {
        super(driver);
        PageFactory.initElements(driver, this);
        this.root = root;
    }


    // relative locator
    private WebElement jobNameDropdown() {
        return root.findElement(
                By.xpath(".//label[contains(.,'Job Name')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement addPositionButton() {
        return root.findElement(
                By.xpath(".//button[contains(.,'+ Add Position')]")
        );
    }

    public void selectJobName(String jobName) {
        selectDropdownModal(jobNameDropdown(), jobName);
    }

    public void clickAddPosition() {
        click(addPositionButton());
    }

    public List<PositionSectionComponent> getPositions() {

        List<WebElement> positions = root.findElements(
                By.xpath(".//span[contains(.,'Position')]/ancestor::div[contains(@class,'border rounded-lg')]")
        );

        List<PositionSectionComponent> result = new ArrayList<>();

        for (WebElement position : positions) {
            result.add(new PositionSectionComponent(driver, position));
        }

        return result;
    }

    public PositionSectionComponent getPosition(int index) {
        return getPositions().get(index);
    }
}