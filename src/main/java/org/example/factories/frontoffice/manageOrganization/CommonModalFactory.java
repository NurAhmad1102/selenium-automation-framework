package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ChangesLogModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ConfirmationActionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.openqa.selenium.WebDriver;

public class CommonModalFactory {
    private final WebDriver driver;
    private ConfirmationActionModal confirmationActionModal;
    private SuccessModal successModal;
    private ChangesLogModal changesLogModal;

    public CommonModalFactory(WebDriver driver) {
        this.driver = driver;
    }

    public ConfirmationActionModal getConfirmationActionModal() {
        if (confirmationActionModal == null) {
            confirmationActionModal = new ConfirmationActionModal(driver);
        }
        return confirmationActionModal;
    }

    public SuccessModal getSuccessModal() {
        if (successModal == null) {
            successModal = new SuccessModal(driver);
        }
        return successModal;
    }

    public ChangesLogModal getChangesLogModal() {
        if (changesLogModal == null) {
            changesLogModal = new ChangesLogModal(driver);
        }
        return changesLogModal;
    }
}