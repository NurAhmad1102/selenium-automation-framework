package org.example.mainPagesFactory;

import org.example.drivers.DriverManager;
import org.example.factories.frontoffice.login.FrontOfficeLoginPagesFactory;
import org.example.factories.frontoffice.manageOrganization.ManageOrganizationFactory;
import org.example.factories.frontoffice.manageOrganization.SetOrganizationModalFactory;
import org.example.factories.frontoffice.sideMenuFactory.FrontOfficeSideMenuFactory;
import org.openqa.selenium.WebDriver;

public class FrontOfficeMainPagesFactory {
    private final WebDriver driver;

    private FrontOfficeLoginPagesFactory frontOfficeLoginPagesFactory;
    private FrontOfficeSideMenuFactory frontOfficeSideMenuFactory;
    private ManageOrganizationFactory manageOrganizationFactory;

    public FrontOfficeMainPagesFactory(WebDriver driver) {
        this.driver = driver;
    }

    public FrontOfficeLoginPagesFactory login() {
        if (frontOfficeLoginPagesFactory == null) {
            frontOfficeLoginPagesFactory = new FrontOfficeLoginPagesFactory(driver);
        }
        return frontOfficeLoginPagesFactory;
    }

    public FrontOfficeSideMenuFactory sideMenu() {
        if (frontOfficeSideMenuFactory == null) {
            frontOfficeSideMenuFactory = new FrontOfficeSideMenuFactory(driver);
        }
        return frontOfficeSideMenuFactory;
    }

    public ManageOrganizationFactory manageOrganization() {
        if (manageOrganizationFactory == null) {
            manageOrganizationFactory = new ManageOrganizationFactory(driver);
        }
        return manageOrganizationFactory;
    }



}
