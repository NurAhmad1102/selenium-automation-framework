package org.example.factories.backoffice.manageOrganization.slaSettingMenu.modalFactory.addSlaDetailModalFactory;

import org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal.ActionSelectorModal;
import org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal.CopyFromPreviousModal;
import org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal.SlaDetailModal;
import org.openqa.selenium.WebDriver;

public class AddSlaDetailModalFactory {
    private WebDriver driver;
    private ActionSelectorModal actionSelectorModal;
    private CopyFromPreviousModal copyFromPreviousModal;
    private SlaDetailModal slaDetailModal;

    public AddSlaDetailModalFactory(WebDriver driver) {
        this.driver = driver;
    }

    public ActionSelectorModal getActionSelectorModal() {
        if(actionSelectorModal == null){
            actionSelectorModal = new ActionSelectorModal(driver);
        }
        return actionSelectorModal;
    }

    public CopyFromPreviousModal getCopyFromPreviousModal() {
        if(copyFromPreviousModal == null){
            copyFromPreviousModal = new CopyFromPreviousModal(driver);
        }
        return copyFromPreviousModal;
    }

    public SlaDetailModal getSlaDetailModal() {
        if (slaDetailModal == null){
            slaDetailModal = new SlaDetailModal(driver);
        }
        return slaDetailModal;
    }
}
