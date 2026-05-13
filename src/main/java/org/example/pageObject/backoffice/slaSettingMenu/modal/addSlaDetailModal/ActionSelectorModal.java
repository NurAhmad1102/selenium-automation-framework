package org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;

public class ActionSelectorModal extends SlaDetailModal{

    public ActionSelectorModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    private final String SINGLE_RADIO = "//input[@type='radio' and @value='%s']";

    private final String MULTIPLE_CHECKBOX = "//input[@type='checkbox' and @value='%s']";

    // Untuk single action (radio button)
    public WebElement getSingleActionRadio(String actionValue) {
        String xpath = String.format(SINGLE_RADIO, actionValue);
        return driver.findElement(By.xpath(xpath));
    }

    public void selectSingleAction(String actionValue) {
        getSingleActionRadio(actionValue).click();
    }

    public boolean isSingleActionSelected(String actionValue) {
        return getSingleActionRadio(actionValue).isSelected();
    }

    // Untuk multiple action (checkbox)
    public WebElement getMultipleActionCheckbox(String actionValue) {
        String xpath = String.format(MULTIPLE_CHECKBOX, actionValue);
        return driver.findElement(By.xpath(xpath));
    }

    public void selectMultipleAction(String actionValue) {
        WebElement checkbox = getMultipleActionCheckbox(actionValue);
        if (!checkbox.isSelected()) {
            checkbox.click();
        }
    }

    public void deselectMultipleAction(String actionValue) {
        WebElement checkbox = getMultipleActionCheckbox(actionValue);
        if (checkbox.isSelected()) {
            checkbox.click();
        }
    }

    public boolean isMultipleActionSelected(String actionValue) {
        return getMultipleActionCheckbox(actionValue).isSelected();
    }
}







