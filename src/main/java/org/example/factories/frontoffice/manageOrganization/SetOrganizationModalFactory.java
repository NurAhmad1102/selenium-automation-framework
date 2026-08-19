package org.example.factories.frontoffice.manageOrganization;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ChangesLogModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ConfirmationActionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.NodeOrganizationModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.PreviewProposedOrg;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal.*;
import org.openqa.selenium.WebDriver;

public class SetOrganizationModalFactory {
    private final WebDriver driver;
    private AddFormModal addFormModal;
    private MoveFormModal moveFormModal;
    private MergeFormModal mergeFormModal;
    private SplitFormModal splitFormModal;
    private RenameFormModal renameFormModal;
    private AddJobPositionModal addJobPositionModal;
    private ConfirmationActionModal confirmationActionModal;
    private SuccessModal successModal;
    private ChangesLogModal changesLogModal;
    private NodeOrganizationModal nodeOrganizationModal;
    private EditJobsPositionsModal editJobsPositionsModal;
    private PreviewProposedOrg previewProposedOrg;

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

    public RenameFormModal getRenameFormModal() {
        if (renameFormModal == null) {
            renameFormModal = new RenameFormModal(driver);
        }
        return renameFormModal;
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

    public ChangesLogModal getChangesLogModal() {
        if (changesLogModal == null) {
            changesLogModal = new ChangesLogModal(driver);
        }
        return changesLogModal;
    }

    public NodeOrganizationModal getNodeOrganizationModal() {
        if (nodeOrganizationModal == null) {
            nodeOrganizationModal = new NodeOrganizationModal(driver);
        }
        return nodeOrganizationModal;
    }

    public EditJobsPositionsModal getEditJobsPositionsModal() {
        if (editJobsPositionsModal == null) {
            editJobsPositionsModal = new EditJobsPositionsModal(driver);
        }
        return editJobsPositionsModal;
    }

    public PreviewProposedOrg getPreviewProposedOrg() {
        if (previewProposedOrg == null) {
            previewProposedOrg = new PreviewProposedOrg(driver);
        }
        return previewProposedOrg;
    }
}
