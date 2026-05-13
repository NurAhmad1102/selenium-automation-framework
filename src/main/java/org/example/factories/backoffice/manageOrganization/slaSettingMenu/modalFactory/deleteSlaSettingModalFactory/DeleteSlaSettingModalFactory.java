package org.example.factories.backoffice.manageOrganization.slaSettingMenu.modalFactory.deleteSlaSettingModalFactory;

import org.example.pageObject.backoffice.slaSettingMenu.modal.deleteSlaSetting.DeleteSlaSettingModal;
import org.openqa.selenium.WebDriver;

public class DeleteSlaSettingModalFactory {
    private WebDriver driver;
    private DeleteSlaSettingModal deleteSlaSettingModal;

    public DeleteSlaSettingModalFactory(WebDriver driver) {
        this.driver = driver;
    }

    public DeleteSlaSettingModal getDeleteSlaSettingModal() {
        if(deleteSlaSettingModal == null){
            deleteSlaSettingModal = new DeleteSlaSettingModal(driver);
        }
        return deleteSlaSettingModal;
    }
}
