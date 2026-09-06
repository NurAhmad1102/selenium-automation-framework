package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.factories.frontoffice.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.manageOrganization.assignEmployeeFlow2.AssignEmployeePage;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.CurrentOrganizationSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.NewJobSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.NewPositionSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.OrganizationChangesJustificationPage;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.ProposedAssignmentEmployeeSection;
import org.example.pageObject.frontoffice.manageOrganization.organizationChangesJustificationFlow3.ProposedOrganizationSection;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.ActionElementsPage;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.example.utils.CustomSoftAssert;

import java.util.function.Consumer;

public class OrganizationChangesJustificationHelper {
    private final Logger logger = LogManager.getLogger(getClass());

    private final OrganizationChangesJustificationPage justificationPage;
    private final NewJobSection newJobSection;
    private final NewPositionSection newPositionSection;
    private final ProposedAssignmentEmployeeSection proposedAssignmentEmployee;
    private final CurrentOrganizationSection currentOrganizationSection;
    private final ProposedOrganizationSection proposedOrganizationSection;

    private final AssignEmployeePage assignEmployeePage;
    private final ActionElementsPage actionElementsPage;
    private final ManageOrganizationHelper manageOrganizationHelper;

    public OrganizationChangesJustificationHelper(FrontOfficeMainPagesFactory mainPagesFactory,
                                                 ManageOrganizationHelper manageOrganizationHelper) {
        this.justificationPage = mainPagesFactory.manageOrganization().getOrganizationChangesJustificationPage();
        this.newJobSection = mainPagesFactory.manageOrganization().justificationSections().getNewJobSection();
        this.newPositionSection = mainPagesFactory.manageOrganization().justificationSections().getNewPositionSection();
        this.proposedAssignmentEmployee = mainPagesFactory.manageOrganization().justificationSections().getProposedAssignmentEmployee();
        this.currentOrganizationSection = mainPagesFactory.manageOrganization().justificationSections().getCurrentOrganizationSection();
        this.proposedOrganizationSection = mainPagesFactory.manageOrganization().justificationSections().getProposedOrganizationSection();

        this.assignEmployeePage = mainPagesFactory.manageOrganization().getAssignEmployeeFlow2Page();
        this.actionElementsPage = mainPagesFactory.manageOrganization().getActionElementsPage();
        this.manageOrganizationHelper = manageOrganizationHelper;
    }

    public OrganizationChangesJustificationPage justificationPage() {
        return justificationPage;
    }

    public NewJobSection newJobSection() {
        return newJobSection;
    }

    public NewPositionSection newPositionSection() {
        return newPositionSection;
    }

    public ProposedAssignmentEmployeeSection proposedAssignmentEmployee() {
        return proposedAssignmentEmployee;
    }

    public CurrentOrganizationSection currentOrganizationSection() {
        return currentOrganizationSection;
    }

    public ProposedOrganizationSection proposedOrganizationSection() {
        return proposedOrganizationSection;
    }

    // ==================== UNIFIED MODULAR E2E VALIDATION FLOW ====================

    public void validateFullJustificationFlow(
            OrganizationTestData flow1Data,
            OrganizationTestData flow2Data,
            boolean isNewJob,
            OrgMenuAction action,
            Consumer<CustomSoftAssert> orgStructureValidation)
    {
        logger.info("Executing Unified Complete Flow 3 End-to-End Validation (Excel Data Match)");

        CustomAssert.assertTrue(justificationPage.isOrganizationChangesJustificationPageDisplayed(),
                "Organization Changes Justification page is not displayed");

        CustomSoftAssert softAssert = new CustomSoftAssert();

        // opsional hanya jika di flow 1 buat new job
        if (isNewJob) {
            validateNewJobSection(softAssert, flow1Data);
        }

        validateNewPositionSection(softAssert, flow1Data, action);

        orgStructureValidation.accept(softAssert);


        validateProposedEmployeeAssignmentSection(softAssert, flow1Data, flow2Data);
        softAssert.assertAll();
        validateJustificationFormAndSubmit();
    }

    // ==================== MODULAR SUB-METHODS ====================

    public void validateNewJobSection(CustomSoftAssert softAssert, OrganizationTestData flow1Data) {
        newJobSection.expandNewJobSection();
        String jobName = flow1Data.getJobName() ;
        String orgName = flow1Data.getNewOrganizationName();

        logger.info("Validating Optional New Job: [{}] in Flow 3", jobName);
        softAssert.assertTrue(newJobSection.isNewJobAndOrganizationDisplayed(jobName, orgName),
                "New Job [" + jobName + "] di " +
                        "Organization ["+ orgName +"] tidak muncul di Flow 3");
    }

    public void validateNewPositionSection(CustomSoftAssert softAssert, OrganizationTestData flow1Data, OrgMenuAction action) {
        newPositionSection.expandNewPositionSection();
        String posName = flow1Data.getJobName();
            logger.info("Validating New Position Name & Details: [{}] in Flow 3", posName);
            softAssert.assertTrue(newPositionSection.isPositionDisplayed(posName), "New Position [" + posName + "] harus ditampilkan di Flow 3");

            // Sub-method validasi 6 atribut detail New Position dari data Excel flow1Data
            validateNewPositionDetails(softAssert, posName, flow1Data, action);
    }

    /**
     * Sub-Method Terpisah: Validasi 6 Atribut Detail New Position dari Excel:
     * Parent Position,
     * Org Name,
     * Job Name,
     * Location,
     * FTE,
     * Position Naming Type
     */
    public void validateNewPositionDetails(CustomSoftAssert softAssert, String posName, OrganizationTestData flow1Data, OrgMenuAction action) {
        newPositionSection.clickPositionItem(posName);

        final String orgNameLabel = "Organization Name";
        final String jobNameLabel = "Job Name";
        final String locationLabel = "Location";
        final String positionNamingTypeLabel = "Position Naming Type";
        final String fteLabel = "FTE";

//        Expected data dari excel
        String expectedParentPosition = flow1Data.getParentPosition();
        String expectedOrg = flow1Data.getNewOrganizationName();
        String expectedJob = flow1Data.getJobName();
        String expectedLocation = flow1Data.getLocation();
        String expectedFTE = flow1Data.getFteNumbers();
        String expectedPositionType = flow1Data.getPositionNamingType();

        String defaultFTE = "1";
        switch (action) {
            case RENAME, MOVE -> expectedFTE = defaultFTE; // tidak ubah FTE
            case MERGE, ADD_NEW -> expectedFTE = flow1Data.getFteNumbers(); // buat FTE baru
            case SPLIT -> {
                if (posName.equals(flow1Data.getJobName2())) {
                    expectedFTE = flow1Data.getFteNumbers();
                } else {
                    expectedFTE = defaultFTE;
                }
            }
        }

//        Actual data dari UI
        String actualParent = newPositionSection.getPositionParentInfo(posName);
        String actualOrg = newPositionSection.getPositionDetailValue(posName, orgNameLabel);
        String actualJob = newPositionSection.getPositionDetailValue(posName, jobNameLabel);
        String actualLoc = newPositionSection.getPositionDetailValue(posName, locationLabel);
        String actualFte = newPositionSection.getPositionDetailValue(posName, fteLabel);
        String actualType = newPositionSection.getPositionDetailValue(posName, positionNamingTypeLabel);

        // 1. Parent Position
            softAssert.assertEquals(actualParent, expectedParentPosition,
                    "Parent Position untuk [" + posName + "] tidak sesuai Excel. " +
                            "Expected: [" + expectedParentPosition + "], Actual: [" + actualParent + "]");


        // 2. Organization Name
            softAssert.assertEquals(actualOrg,expectedOrg,
                    "Organization Name pada New Position [" + posName + "] tidak sesuai Excel. " +
                            "Expected: [" + expectedOrg + "], Actual: [" + actualOrg + "]");

        // 3. Job Name
            softAssert.assertEquals(actualJob,expectedJob,
                    "Job Name pada New Position [" + posName + "] tidak sesuai Excel. " +
                            "Expected: [" + expectedJob + "], Actual: [" + actualJob + "]");

        // 4. Location
            softAssert.assertEquals(actualLoc, expectedLocation,
                    "Location pada New Position [" + posName + "] tidak sesuai Excel. " +
                            "Expected: [" + expectedLocation + "], Actual: [" + actualLoc + "]");

        // 5. FTE
            softAssert.assertEquals(actualFte, expectedFTE,
                    "FTE pada New Position [" + posName + "] tidak sesuai Excel. " +
                            "Expected: [" + expectedFTE + "], Actual: [" + actualFte + "]");

        // 6. Position Naming Type
            softAssert.assertEquals(actualType, expectedPositionType,
                    "Position Naming Type pada New Position [" + posName + "] tidak sesuai Excel. " +
                            "Expected: [" + expectedPositionType + "], Actual: [" + actualType + "]");

    }

    public void validateProposedEmployeeAssignmentSection(
            CustomSoftAssert softAssert,
            OrganizationTestData flow1Data,
            OrganizationTestData flow2Data)
    {
        validateProposedEmployeeAssignmentSection(softAssert, flow1Data, flow2Data, flow2Data.isPJS());
    }

    public void validateProposedEmployeeAssignmentSection(
            CustomSoftAssert softAssert,
            OrganizationTestData flow1Data,
            OrganizationTestData flow2Data,
            boolean isPJS)
    {
        proposedAssignmentEmployee.isProposedAssignmentEmployeeSectionDisplayed();
        proposedAssignmentEmployee.expandProposedAssignmentSection();
        String empNip = flow2Data.getNip();
        String assignmentType = isPJS ? "PJS Assignment" : "Primary Assignment";
        logger.info("Validating Proposed Employee Assignment NIP: [{}] in Flow 3 as {}", empNip, assignmentType);

        String expectedMovementCategory = isPJS
                ? "Temporary Assignment"
                : flow2Data.getMovementCategory();

        softAssert.assertTrue(proposedAssignmentEmployee.isEmployeeAssignmentDisplayed(empNip, expectedMovementCategory),
                "Employee [" + empNip + "] dengan Movement Category [" + expectedMovementCategory + "] tidak ada di Proposed Employee Assignment Flow 3");

        String actualMovementCategoryText = proposedAssignmentEmployee.getMovementCategory(empNip, expectedMovementCategory);
        softAssert.assertTrue(actualMovementCategoryText.contains(expectedMovementCategory),
                "Movement Category pada Proposed Employee Assignment tidak sesuai. " +
                        "Expected memuat: [" + expectedMovementCategory + "], Actual: [" + actualMovementCategoryText + "]");

        // Sub-method pencocokan langsung 4 baris tabel detail dari Excel
        validateEmployeeAssignmentTableDetails(softAssert, empNip, expectedMovementCategory, flow1Data, flow2Data);
    }

    /**
     * Sub-Method 4: Validasi Langsung Ke-4 Baris Tabel (Current vs Proposed) dari Excel Data
     * Baris 1: Organization Level
     * Baris 2: Organization Name
     * Baris 3: Job Name
     * Baris 4: Position Name
     */
    public void validateEmployeeAssignmentTableDetails(CustomSoftAssert softAssert, String empNip, OrganizationTestData flow1Data, OrganizationTestData flow2Data) {
        String movementCategory = flow2Data.isPJS() ? "Temporary Assignment" : flow2Data.getMovementCategory();
        validateEmployeeAssignmentTableDetails(softAssert, empNip, movementCategory, flow1Data, flow2Data);
    }

    public void validateEmployeeAssignmentTableDetails(CustomSoftAssert softAssert, String empNip, String movementCategory, OrganizationTestData flow1Data, OrganizationTestData flow2Data) {
        proposedAssignmentEmployee.clickEmployeeItem(empNip, movementCategory);

//        COllumn
        final String colProposed = "Proposed";
        final String colCurrent = "Current";

//        Detail row
        final String orgLevelRow = "Organization Level";
        final String orgNameRow = "Organization Name";
        final String jobNameRow = "Job Name";
        final String positionNameRow = "Position Name";

        // Current Employee data
        String expectedCurrentOrgLevel = flow2Data.getCurrentOrgLevel();
        String expectedCurrentOrgName = flow2Data.getCurrentOrg();
        String expectedCurrentJob = flow2Data.getCurrentJob();
        String expectedCurrentPosition = flow2Data.getCurrentPosition();

        // Proposed Employee data
        String expectedProposedOrgLevel = flow1Data.getLevel();
        String expectedProposedOrgName = flow1Data.getNewOrganizationName();
        String expectedProposedJob = flow1Data.getJobName();
//        TODO: krena naming type = same as job, jika diubah maka ubah jadi position name
        String expectedProposedPosition = expectedProposedJob;

        String actualCurrentLevel = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, orgLevelRow, colCurrent);
        String actualProposedLevel = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, orgLevelRow, colProposed);

//        ORG Level
        softAssert.assertEquals(actualCurrentLevel, expectedCurrentOrgLevel,
                "Current Org Level tidak sesuai. " +
                        "Expected: [" + expectedCurrentOrgLevel + "], Actual: [" + actualCurrentLevel + "]");

        softAssert.assertEquals(actualProposedLevel, expectedProposedOrgLevel,
                "Proposed Org Level tidak sesuai. " +
                        "Expected: [" + expectedProposedOrgLevel + "], Actual: [" + actualProposedLevel + "]");

//        ORG NAME
        String actualCurrentOrg = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, orgNameRow, colCurrent);
        String actualProposedOrg = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, orgNameRow, colProposed);

        softAssert.assertEquals(actualCurrentOrg, expectedCurrentOrgName,
                "Current Org Name tidak sesuai. " +
                        "Expected: [" + expectedCurrentOrgName + "], Actual: [" + actualCurrentOrg + "]");

        softAssert.assertEquals(actualProposedOrg, expectedProposedOrgName,
                "Proposed Org Name tidak sesuai. " +
                        "Expected: [" + expectedProposedOrgName + "], Actual: [" + actualProposedOrg + "]");

//      JOB NAME
        String actualCurrentJob = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, jobNameRow, colCurrent);
        String actualProposedJob = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, jobNameRow, colProposed);

        softAssert.assertEquals(actualCurrentJob, expectedCurrentJob,
                "Current Job Name tidak sesuai. " +
                        "Expected: [" + expectedCurrentJob + "], Actual: [" + actualCurrentJob + "]");

        softAssert.assertEquals(actualProposedJob, expectedProposedJob,
                "Proposed Job Name tidak sesuai. " +
                        "Expected: [" + expectedProposedJob + "], Actual: [" + actualProposedJob + "]");

//    POSITION NAME
        String actualCurrentPos = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, positionNameRow, colCurrent);
        String actualProposedPos = proposedAssignmentEmployee.getAssignmentDetailValue(empNip, movementCategory, positionNameRow, colProposed);

        softAssert.assertEquals(actualCurrentPos, expectedCurrentPosition,
                "Current Position Name tidak sesuai. " +
                        "Expected: [" + expectedCurrentPosition + "], Actual: [" + actualCurrentPos + "]");

        softAssert.assertEquals(actualProposedPos, expectedProposedPosition,
                "Proposed Position Name tidak sesuai. " +
                        "Expected: [" + expectedProposedPosition + "], Actual: [" + actualProposedPos + "]");
    }

    public void validateJustificationFormAndSubmit() {
        justificationPage.setBackgroundJustification("Automated E2E Test Background Justification");
        justificationPage.setRequestorNotes("Automated E2E Test Requestor Notes");
        justificationPage.clickSubmitButton();
    }

    // ==================== ACTION-SPECIFIC E2E FLOW WRAPPERS ====================

    public void validateAddOrganizationJustificationFlow(OrganizationTestData flow1Data, OrganizationTestData flow2Data, boolean isNewJob) {
        validateFullJustificationFlow(flow1Data, flow2Data, isNewJob, OrgMenuAction.ADD_NEW,softAssert -> {
            verifyAddOrganizationStructureInJustification(softAssert, flow1Data.getPrimaryOrganization(), flow1Data.getNewOrganizationName());
        });
    }

    public void validateMergeOrganizationJustificationFlow(OrganizationTestData flow1Data, OrganizationTestData flow2Data, boolean isNewJob) {
        validateFullJustificationFlow(flow1Data, flow2Data, isNewJob, OrgMenuAction.MERGE, softAssert -> {
            verifyMergeOrganizationStructureInJustification(softAssert, flow1Data.getPrimaryOrganization(), flow1Data.getMergeOrganizationWith(), flow1Data.getNewOrganizationName());
        });
    }

    public void validateSplitOrganizationJustificationFlow(OrganizationTestData flow1Data, OrganizationTestData flow2Data, boolean isNewJob) {
        validateFullJustificationFlow(flow1Data, flow2Data, isNewJob, OrgMenuAction.SPLIT, softAssert -> {
            verifySplitOrganizationStructureInJustification(softAssert, flow1Data.getPrimaryOrganization(), flow1Data.getNewOrganizationName1(), flow1Data.getNewOrganizationName2());
        });
    }

    public void validateRenameOrganizationJustificationFlow(OrganizationTestData flow1Data, OrganizationTestData flow2Data, boolean isNewJob) {
        validateFullJustificationFlow(flow1Data, flow2Data, isNewJob, OrgMenuAction.RENAME, softAssert -> {
            verifyRenameOrganizationStructureInJustification(softAssert, flow1Data.getPrimaryOrganization(), flow1Data.getNewOrganizationName());
        });
    }

    public void validateMoveOrganizationJustificationFlow(OrganizationTestData flow1Data, OrganizationTestData flow2Data, boolean isNewJob) {
        validateFullJustificationFlow(flow1Data, flow2Data, isNewJob, OrgMenuAction.MOVE, softAssert -> {
            verifyMoveOrganizationStructureInJustification(softAssert, flow1Data.getPrimaryOrganization(), flow1Data.getNewOrganizationName());
        });
    }

    public void validateRemoveOrganizationJustificationFlow(OrganizationTestData flow1Data, OrganizationTestData flow2Data) {
        validateFullJustificationFlow(flow1Data, flow2Data, false, OrgMenuAction.REMOVE, softAssert -> {
            verifyRemoveOrganizationStructureInJustification(softAssert, flow1Data.getPrimaryOrganization());
        });
    }

    // ==================== CURRENT VS PROPOSED STRUCTURE VALIDATIONS PER ACTION ====================

    public void verifyAddOrganizationStructureInJustification(CustomSoftAssert softAssert, String parentOrg, String newOrg) {
        logger.info("Validating ADD NEW Org Structure - Current vs Proposed: Parent [{}] -> New [{}]", parentOrg, newOrg);
        
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeDisplayedInCurrentOrg(parentOrg),
                "Current Org: Parent Organization [" + parentOrg + "] harus ada di Current Org Structure");
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeInvisibleInCurrentOrg(newOrg),
                "Current Org: New Organization [" + newOrg + "] TIDAK boleh ada di Current Org Structure");

        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeDisplayedInProposedOrg(parentOrg),
                "Proposed Org: Parent Organization [" + parentOrg + "] harus ada di Proposed Org Structure");
        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeDisplayedInProposedOrg(newOrg),
                "Proposed Org: New Organization [" + newOrg + "] HARUS ada di Proposed Org Structure");
    }

    public void verifySplitOrganizationStructureInJustification(CustomSoftAssert softAssert, String sourceOrg, String newSplitOrg1, String newSplitOrg2) {
        logger.info("Validating SPLIT Org Structure - Current vs Proposed: Source [{}] -> Split 1 [{}] & Split 2 [{}]", sourceOrg, newSplitOrg1, newSplitOrg2);

        softAssert.assertTrue(currentOrganizationSection.isOrgNodeDisplayedInCurrentOrg(sourceOrg),
                "Current Org: Source Organization [" + sourceOrg + "] harus ada di Current Org Structure");
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeInvisibleInCurrentOrg(newSplitOrg1),
                "Current Org: Split Org 1 [" + newSplitOrg1 + "] TIDAK boleh ada di Current Org Structure");
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeInvisibleInCurrentOrg(newSplitOrg2),
                "Current Org: Split Org 2 [" + newSplitOrg2 + "] TIDAK boleh ada di Current Org Structure");

        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeInvisibleInProposedOrg(sourceOrg),
                "Proposed Org: Source Organization [" + sourceOrg + "] TIDAK boleh ada (telah di-split) di Proposed Org Structure");
        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeDisplayedInProposedOrg(newSplitOrg1),
                "Proposed Org: Split Org 1 [" + newSplitOrg1 + "] HARUS ada di Proposed Org Structure");
        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeDisplayedInProposedOrg(newSplitOrg2),
                "Proposed Org: Split Org 2 [" + newSplitOrg2 + "] HARUS ada di Proposed Org Structure");
    }

    public void verifyMergeOrganizationStructureInJustification(CustomSoftAssert softAssert, String primaryOrg, String mergeWithOrg, String newMergedOrg) {
        logger.info("Validating MERGE Org Structure - Current vs Proposed: Primary [{}] + MergeWith [{}] -> NewMerged [{}]", primaryOrg, mergeWithOrg, newMergedOrg);

        softAssert.assertTrue(currentOrganizationSection.isOrgNodeDisplayedInCurrentOrg(primaryOrg),
                "Current Org: Primary Organization [" + primaryOrg + "] harus ada di Current Org Structure");
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeDisplayedInCurrentOrg(mergeWithOrg),
                "Current Org: Merge With Organization [" + mergeWithOrg + "] harus ada di Current Org Structure");
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeInvisibleInCurrentOrg(newMergedOrg),
                "Current Org: New Merged Organization [" + newMergedOrg + "] TIDAK boleh ada di Current Org Structure");

        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeDisplayedInProposedOrg(newMergedOrg),
                "Proposed Org: New Merged Organization [" + newMergedOrg + "] HARUS ada di Proposed Org Structure");
    }

    public void verifyRenameOrganizationStructureInJustification(CustomSoftAssert softAssert, String oldOrgName, String newOrgName) {
        logger.info("Validating RENAME Org Structure - Current vs Proposed: Old [{}] -> New [{}]", oldOrgName, newOrgName);

        softAssert.assertTrue(currentOrganizationSection.isOrgNodeDisplayedInCurrentOrg(oldOrgName),
                "Current Org: Old Organization Name [" + oldOrgName + "] harus ada di Current Org Structure");
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeInvisibleInCurrentOrg(newOrgName),
                "Current Org: New Organization Name [" + newOrgName + "] TIDAK boleh ada di Current Org Structure");

        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeInvisibleInProposedOrg(oldOrgName),
                "Proposed Org: Old Organization Name [" + oldOrgName + "] TIDAK boleh ada di Proposed Org Structure");
        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeDisplayedInProposedOrg(newOrgName),
                "Proposed Org: New Organization Name [" + newOrgName + "] HARUS ada di Proposed Org Structure");
    }

    public void verifyMoveOrganizationStructureInJustification(CustomSoftAssert softAssert, String oldOrgName, String newOrgName) {
        logger.info("Validating MOVE Org Structure - Current vs Proposed: Old [{}] -> New [{}]", oldOrgName, newOrgName);

        softAssert.assertTrue(currentOrganizationSection.isOrgNodeDisplayedInCurrentOrg(oldOrgName),
                "Current Org: Old Organization Name [" + oldOrgName + "] harus ada di Current Org Structure");
        softAssert.assertTrue(currentOrganizationSection.isOrgNodeInvisibleInCurrentOrg(newOrgName),
                "Current Org: New Organization Name [" + newOrgName + "] TIDAK boleh ada di Current Org Structure");

        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeInvisibleInProposedOrg(oldOrgName),
                "Proposed Org: Old Organization Name [" + oldOrgName + "] TIDAK boleh ada di Proposed Org Structure");
        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeDisplayedInProposedOrg(newOrgName),
                "Proposed Org: New Organization Name [" + newOrgName + "] HARUS ada di Proposed Org Structure");
    }

    public void verifyRemoveOrganizationStructureInJustification(CustomSoftAssert softAssert, String removedOrg) {
        logger.info("Validating REMOVE Org Structure - Current vs Proposed: Removed Org [{}]", removedOrg);

        softAssert.assertTrue(currentOrganizationSection.isOrgNodeDisplayedInCurrentOrg(removedOrg),
                "Current Org: Removed Organization [" + removedOrg + "] harus ada di Current Org Structure");

        softAssert.assertTrue(proposedOrganizationSection.isOrgNodeInvisibleInProposedOrg(removedOrg),
                "Proposed Org: Removed Organization [" + removedOrg + "] TIDAK boleh ada (telah dihapus) di Proposed Org Structure");
    }
}
