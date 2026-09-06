package org.example.factories.backoffice.manageOrganization.slaSettingMenu.slaSettingFactory;

import org.example.pageObject.backoffice.slaSettingMenu.slasetting.SlaSettingDetail;
import org.example.pageObject.backoffice.slaSettingMenu.slasetting.SlaSettingPage;
import org.openqa.selenium.WebDriver;

public class SlaSettingFactory {
    private WebDriver driver;
    private SlaSettingDetail slaSettingDetail;
    private SlaSettingPage slaSettingPage;

    public SlaSettingFactory(WebDriver driver) {
        this.driver = driver;
    }

    public SlaSettingDetail getSlaSettingDetail() {
        if (slaSettingDetail == null){
            slaSettingDetail = new SlaSettingDetail(driver);
        }
        return slaSettingDetail;
    }

    public SlaSettingPage getSlaSettingPage() {
        if (slaSettingPage == null){
            slaSettingPage = new SlaSettingPage(driver);
        }
        return slaSettingPage;
    }
}
