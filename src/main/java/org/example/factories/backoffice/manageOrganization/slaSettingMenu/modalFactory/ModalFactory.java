package org.example.factories.backoffice.manageOrganization.slaSettingMenu.modalFactory;

import org.example.factories.backoffice.manageOrganization.slaSettingMenu.modalFactory.addSlaDetailModalFactory.AddSlaDetailModalFactory;
import org.example.factories.backoffice.manageOrganization.slaSettingMenu.modalFactory.deleteSlaSettingModalFactory.DeleteSlaSettingModalFactory;
import org.openqa.selenium.WebDriver;

public class ModalFactory {
    private WebDriver driver;
    private AddSlaDetailModalFactory addSlaDetailModalFactory;
    private DeleteSlaSettingModalFactory deleteSlaSettingModalFactory;

    public ModalFactory(WebDriver driver) {
        this.driver = driver;
    }

    public AddSlaDetailModalFactory getAddSlaDetailModalFactory() {
        if(addSlaDetailModalFactory == null){
            addSlaDetailModalFactory = new AddSlaDetailModalFactory(driver);
        }
        return addSlaDetailModalFactory;
    }

    public DeleteSlaSettingModalFactory getDeleteSlaSettingModalFactory() {
        if(deleteSlaSettingModalFactory == null){
            deleteSlaSettingModalFactory = new DeleteSlaSettingModalFactory(driver);
        }
        return deleteSlaSettingModalFactory;
    }
}
