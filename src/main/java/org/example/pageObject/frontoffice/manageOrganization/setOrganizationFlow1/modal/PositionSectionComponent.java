package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class PositionSectionComponent extends BasePage {
    private final WebElement root;

    public PositionSectionComponent(WebDriver driver, WebElement root) {
        super(driver);
        PageFactory.initElements(driver, this);
        this.root = root;
    }

    private WebElement positionNamingTypeDropdown(){
        return root.findElement(
                By.xpath(".//label[contains(.,'Position Naming Type')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement locationDropdown(){
        return root.findElement(
                By.xpath(".//label[contains(.,'Location')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement parentPositionNameDropdown() {
        return root.findElement(
                By.xpath(".//label[contains(.,'Parent Position Name')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement dottedMatrixDropdown() {
        return root.findElement(
                By.xpath(".//label[contains(.,'Dotted Matrix')]/following::button[@role='combobox'][1]")
        );
    }

    private WebElement fteField() {
        return root.findElement(
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
