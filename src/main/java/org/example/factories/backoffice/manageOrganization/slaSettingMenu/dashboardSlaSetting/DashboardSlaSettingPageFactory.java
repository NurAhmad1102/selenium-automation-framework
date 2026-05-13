package org.example.factories.backoffice.manageOrganization.slaSettingMenu.dashboardSlaSetting;

import org.example.pageObject.backoffice.slaSettingMenu.DashboardSlaSettingPage;
import org.openqa.selenium.WebDriver;

public class DashboardSlaSettingPageFactory {
    private WebDriver driver;
    private DashboardSlaSettingPage dashboardSlaSettingPage;

    public DashboardSlaSettingPageFactory(WebDriver driver) {
        this.driver = driver;
    }

    public DashboardSlaSettingPage getDashboardSlaSettingPage() {
        if(dashboardSlaSettingPage == null){
            dashboardSlaSettingPage = new DashboardSlaSettingPage(driver);
        }
        return dashboardSlaSettingPage;
    }
}
