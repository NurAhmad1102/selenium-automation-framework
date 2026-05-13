package org.example.factories.backoffice.sideMenu;

import org.example.pageObject.backoffice.sideMenu.BackOfficeSideMenu;
import org.openqa.selenium.WebDriver;

public class SideMenuFactory {
    private WebDriver driver;
    private BackOfficeSideMenu backOfficeSideMenu;

    public SideMenuFactory(WebDriver driver) {
        this.driver = driver;
    }

    public BackOfficeSideMenu getSideMenu() {
        if(backOfficeSideMenu == null){
            backOfficeSideMenu = new BackOfficeSideMenu(driver);
        }
        return backOfficeSideMenu;
    }
}
