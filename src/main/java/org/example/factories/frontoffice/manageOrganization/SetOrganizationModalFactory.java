package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.*;
import org.openqa.selenium.WebDriver;

public class SetOrganizationModalFactory {
    private final WebDriver driver;
    private AddFormModal addFormModal;
    private MoveFormModal moveFormModal;
    private MergeFormModal mergeFormModal;
    private SplitFormModal splitFormModal;
    private AddJobPositionModal addJobPositionModal;
    private ConfirmationActionModal confirmationActionModal;
    private SuccessModal successModal;

    public SetOrganizationModalFactory(WebDriver driver) {
        this.driver = driver;
    }

    public AddFormModal getAddFormModal() {
        if (addFormModal == null) {
            addFormModal = new AddFormModal(driver);
        }
        return addFormModal;
    }

    public MoveFormModal getMoveFormModal() {
        if (moveFormModal == null) {
            moveFormModal = new MoveFormModal(driver);
        }
        return moveFormModal;
    }

    public MergeFormModal getMergeFormModal() {
        if (mergeFormModal == null) {
            mergeFormModal = new MergeFormModal(driver);
        }
        return mergeFormModal;
    }

    public SplitFormModal getSplitFormModal() {
        if (splitFormModal == null) {
            splitFormModal = new SplitFormModal(driver);
        }
        return splitFormModal;
    }

    public AddJobPositionModal getAddJobPositionModal() {
        if (addJobPositionModal == null) {
            addJobPositionModal = new AddJobPositionModal(driver);
        }
        return addJobPositionModal;
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
}
