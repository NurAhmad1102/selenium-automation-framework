package org.example.pageObject.backoffice.slaSettingMenu.popUp;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class AlertPopUpMessage extends BasePage {
    public AlertPopUpMessage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[text()='SLA details cannot be empty']")
    private WebElement alertSubmitSlaSettingEmpty;

    @FindBy(xpath = "//div[starts-with(text(),\"SLA period with name\") and contains(text(),\"already exists\")]")
    private WebElement alertDuplicatePeriodeName;

    public boolean isAlertSubmitSlaSettingEmpty(){
        return isVisible(alertSubmitSlaSettingEmpty);
    }

    public boolean isAlertDuplicatePeriodeName(){
        return isVisible(alertDuplicatePeriodeName);
    }
}
