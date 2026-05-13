package org.example.factories.backoffice.manageOrganization;

import org.example.factories.backoffice.manageOrganization.slaSettingMenu.dashboardSlaSetting.DashboardSlaSettingPageFactory;
import org.example.factories.backoffice.manageOrganization.slaSettingMenu.modalFactory.ModalFactory;
import org.example.factories.backoffice.manageOrganization.slaSettingMenu.popUpFactory.PopUpFactory;
import org.example.factories.backoffice.manageOrganization.slaSettingMenu.slaSettingFactory.SlaSettingFactory;
import org.openqa.selenium.WebDriver;

public class ManageOrganizationFactory {
    private WebDriver driver;
    private DashboardSlaSettingPageFactory dashboardSlaSettingPageFactory;
    private PopUpFactory popUpFactory;
    private SlaSettingFactory slaSettingFactory;
    private ModalFactory modalFactory;

    public ManageOrganizationFactory(WebDriver driver) {
        this.driver = driver;
    }

    public DashboardSlaSettingPageFactory getDashboardSlaSettingPageFactory() {
        if (dashboardSlaSettingPageFactory == null){
            dashboardSlaSettingPageFactory = new DashboardSlaSettingPageFactory(driver);
        }
        return dashboardSlaSettingPageFactory;
    }

    public PopUpFactory getPopUpFactory() {
        if (popUpFactory == null){
            popUpFactory = new PopUpFactory(driver);
        }
        return popUpFactory;
    }

    public SlaSettingFactory getSlaSettingFactory() {
        if(slaSettingFactory == null){
            slaSettingFactory = new SlaSettingFactory(driver);
        }
        return slaSettingFactory;
    }

    public ModalFactory getModalFactory() {
        if (modalFactory == null){
            modalFactory = new ModalFactory(driver);
        }
        return modalFactory;
    }
}
