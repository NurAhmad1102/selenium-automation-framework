package org.example.factories.backoffice.manageOrganization.slaSettingMenu.popUpFactory;

import org.example.pageObject.backoffice.slaSettingMenu.popUp.AlertPopUpMessage;
import org.openqa.selenium.WebDriver;

public class PopUpFactory {
    private WebDriver driver;
    private AlertPopUpMessage alertPopUpMessage;

    public PopUpFactory(WebDriver driver) {
        this.driver = driver;
    }

    public AlertPopUpMessage getAlertPopUpMessage() {
        if(alertPopUpMessage == null){
            alertPopUpMessage = new AlertPopUpMessage(driver);
        }
        return alertPopUpMessage;
    }
}
