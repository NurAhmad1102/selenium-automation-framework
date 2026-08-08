package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SplitFormModal extends BaseFormModal {

    private final By org1NameInput = By.xpath("//input[@name='organizations.0.orgName']");
    private final By org1RcDropdown = By.xpath("//input[@name='organizations.0.orgName']/following::button[@role='combobox'][1]");

    private final By org2NameInput = By.xpath("//input[@name='organizations.1.orgName']");
    private final By org2RcDropdown = By.xpath("//input[@name='organizations.1.orgName']/following::button[@role='combobox'][1]");

    public SplitFormModal(WebDriver driver) {
        super(driver, "Split Organization");
    }

    public boolean isSplitFormHeaderVisible(){
        return isModalHeaderDisplayed();
    }

    public void expandAccordion(String orgTitle) {
        By accordionBtn = By.xpath(String.format("//button[contains(normalize-space(), '%s') and @aria-controls]", orgTitle));
        WebElement btn = wait.until(ExpectedConditions.presenceOfElementLocated(accordionBtn));
        String state = btn.getAttribute("data-state");
        if ("closed".equalsIgnoreCase(state) || "false".equalsIgnoreCase(btn.getAttribute("aria-expanded"))) {
            logger.info("Step: Expanding Accordion for {}", orgTitle);
            click(btn);
        } else {
            logger.info("Step: Accordion {} is already expanded", orgTitle);
        }
    }

    public void fillOrganization1Details(String newName, String rcCode) {
        expandAccordion("Organization 1");
        logger.info("Step: Setting Organization 1 Name to: {}", newName);
        WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(org1NameInput));
        typeText(nameElement, newName);

        logger.info("Step: Selecting RC Code for Organization 1: {}", rcCode);
        WebElement rcElement = wait.until(ExpectedConditions.elementToBeClickable(org1RcDropdown));
        selectDropdownModal(rcElement, rcCode);
    }

    public void fillOrganization2Details(String newName, String rcCode) {
        expandAccordion("Organization 2");
        logger.info("Step: Setting Organization 2 Name to: {}", newName);
        WebElement nameElement = wait.until(ExpectedConditions.visibilityOfElementLocated(org2NameInput));
        typeText(nameElement, newName);

        logger.info("Step: Selecting RC Code for Organization 2: {}", rcCode);
        WebElement rcElement = wait.until(ExpectedConditions.elementToBeClickable(org2RcDropdown));
        selectDropdownModal(rcElement, rcCode);
    }
}
