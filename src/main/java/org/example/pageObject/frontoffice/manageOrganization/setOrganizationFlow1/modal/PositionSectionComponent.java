package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

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
            List<WebElement> positions = parentRootSupplier.get().findElements(
                    By.xpath(".//span[contains(.,'Position')]/ancestor::div[contains(@class,'border rounded-lg')]")
            );
            root = positions.get(index);
            return root;
        }
    }

    private WebElement positionNamingTypeDropdown(){
        return getFreshRoot().findElement(
                By.xpath(".//label[contains(.,'Position Naming Type')]/following::button[@role='combobox'][1]")
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

    private WebElement fteField() {
        return getFreshRoot().findElement(
                By.xpath(".//input[contains(@name,'.mpp')]")
        );
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
