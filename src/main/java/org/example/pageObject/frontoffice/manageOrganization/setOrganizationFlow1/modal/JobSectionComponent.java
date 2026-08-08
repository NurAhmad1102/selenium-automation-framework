package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

import java.util.ArrayList;
import java.util.List;

public class JobSectionComponent extends BasePage {
    private WebElement root;
    private final int index;

    public JobSectionComponent(WebDriver driver, WebElement root, int index) {
        super(driver);
        PageFactory.initElements(driver, this);
        this.root = root;
        this.index = index;
    }

    private WebElement getFreshRoot() {
        try {
            root.isDisplayed();
            return root;
        } catch (StaleElementReferenceException e) {
            logger.debug("JobSection root is stale, re-locating at index {}", index);
            String xpath = "(//div[@role='tabpanel' and @data-state='active'] | //div[@role='dialog'])" +
                           "//*[self::span or self::button][contains(normalize-space(.),'Job & Position')]" +
                           "/ancestor::div[contains(@class,'border') and contains(@class,'rounded')][1]";
            List<WebElement> sections = driver.findElements(By.xpath(xpath));
            root = sections.get(index);
            return root;
        }
    }


    // relative locator
    private WebElement jobNameDropdown() {
        return getFreshRoot().findElement(
                By.xpath(".//label[contains(.,'Job Name')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement addPositionButton() {
        return getFreshRoot().findElement(
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
        String xpath = ".//*[self::span or self::button][contains(normalize-space(.),'Position')]" +
                       "/ancestor::div[contains(@class,'border') and contains(@class,'rounded')][1]";

        List<WebElement> positions = getFreshRoot().findElements(By.xpath(xpath));

        List<PositionSectionComponent> result = new ArrayList<>();

        for (int i = 0; i < positions.size(); i++) {
            result.add(new PositionSectionComponent(driver, positions.get(i), i, this::getFreshRoot));
        }

        return result;
    }

    public PositionSectionComponent getPosition(int index) {
        return getPositions().get(index);
    }
}