package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.util.List;
import java.util.function.Supplier;

public class PositionSectionComponent extends BasePage {
    private WebElement root;
    private final int index;
    private final Supplier<WebElement> parentRootSupplier;

    public PositionSectionComponent(WebDriver driver, WebElement root, int index, Supplier<WebElement> parentRootSupplier) {
        super(driver);
        PageFactory.initElements(driver, this);
        this.root = root;
        this.index = index;
        this.parentRootSupplier = parentRootSupplier;
    }

    private WebElement getFreshRoot() {
        try {
            root.isDisplayed();
            return root;
        } catch (StaleElementReferenceException e) {
            logger.debug("PositionSection root is stale, re-locating at index {}", index);
            String xpath = ".//*[self::span or self::button][contains(normalize-space(.),'Position')]" +
                           "/ancestor::div[contains(@class,'border') and contains(@class,'rounded')][1]";
            List<WebElement> positions = parentRootSupplier.get().findElements(By.xpath(xpath));
            root = positions.get(index);
            return root;
        }
    }

    private WebElement positionNamingTypeDropdown(){
        return getFreshRoot().findElement(
//                By.xpath(".//label[contains(.,'Position Naming Type')]/following::button[@role='combobox'][1]") //TODO: activekan kembali after fix bug https://aktiv.clickup.com/t/86ext2a5r
                By.xpath(".//label[contains(.,'Position Type') or contains(.,'Position Naming Type')]/following::button[@role='combobox'][1]")

        );
    }

    private WebElement locationDropdown(){
        return getFreshRoot().findElement(
                By.xpath(".//label[contains(.,'Location')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement parentPositionNameDropdown() {
        return getFreshRoot().findElement(
                By.xpath(".//label[contains(.,'Parent Position Name')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement dottedMatrixDropdown() {
        return getFreshRoot().findElement(
                By.xpath(".//label[contains(.,'Dotted Matrix')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement positionNameField() {
        return getFreshRoot().findElement(
                By.xpath(".//label[contains(.,'Position Name') or contains(.,'Area of Responsibility') or contains(.,'Job Scope') or contains(.,'Position')]/following::input[not(contains(@name,'.mpp'))][1]")
        );
    }

    private WebElement fteField() {
        return getFreshRoot().findElement(
                By.xpath(".//input[contains(@name,'.mpp')]")
        );
    }

    public void setPositionName(String positionName) {
        logger.info("Step: Setting Position Name to: {}", positionName);
        WebElement element = positionNameField();
        wait.until(ExpectedConditions.visibilityOf(element));
        click(element);

        // 1. Thoroughly clear existing text via keyboard and JS
        element.sendKeys(org.openqa.selenium.Keys.chord(org.openqa.selenium.Keys.CONTROL, "a"), org.openqa.selenium.Keys.BACK_SPACE);
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = '';" +
                    "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));",
                    element
            );
        } catch (Exception ignored) {}

        // 2. Type the new custom position name
        element.sendKeys(positionName);

        // 3. Dispatch events so React commits the custom position name to form state
        try {
            ((org.openqa.selenium.JavascriptExecutor) driver).executeScript(
                    "arguments[0].dispatchEvent(new Event('input', { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('change', { bubbles: true }));" +
                    "arguments[0].dispatchEvent(new Event('blur', { bubbles: true }));",
                    element
            );
        } catch (Exception ignored) {}

        logger.info("Verified Position Name input value: [{}]", element.getAttribute("value"));
    }

    public void selectPositionNamingType(String value){
        selectDropdownModal(positionNamingTypeDropdown(), value);
    }

    public void selectLocation(String value){
        selectDropdownModal(locationDropdown(), value);
    }

    public void selectParentPositionName(String value) {
        selectDropdownModal(parentPositionNameDropdown(), value);
    }

    public void selectDottedMatrix(boolean isDottedMatrix) {
        String value = isDottedMatrix ? "Yes" : "No";
        selectDropdownModal(dottedMatrixDropdown(), value);
    }

    public void setFTE(String value) {
        typeText(fteField(), value);
    }
}
