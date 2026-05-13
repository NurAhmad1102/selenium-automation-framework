package org.example.factories.frontoffice.sideMenuFactory;

import org.example.pageObject.frontoffice.frontOfficeSideMenu.FrontOfficeSideMenu;
import org.openqa.selenium.WebDriver;

public class FrontOfficeSideMenuFactory {
    private WebDriver driver;
    private FrontOfficeSideMenu frontOfficeSideMenu;

    public FrontOfficeSideMenuFactory(WebDriver driver) {
        this.driver = driver;
    }

    public FrontOfficeSideMenu getFrontOfficeSideMenu() {
        if (frontOfficeSideMenu == null) {
            frontOfficeSideMenu = new FrontOfficeSideMenu(driver);
        }
        return frontOfficeSideMenu;
    }
}
