package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.annotations.Test;

public class MultiActionOrganizationTest extends ManageOrganizationBaseTest {

    /**
     * Helper method internal untuk membuat data Organization 2 pada aksi Split.
     * Mengambil JOB_NAME_2 langsung dari data baris Excel terkait, atau fallback ke JOB_POSITION.
     */
    private OrganizationTestData getSecondarySplitData(OrganizationTestData baseData) {
        OrganizationTestData secondaryData = new OrganizationTestData(baseData);
        if (baseData.getJobName2() != null && !baseData.getJobName2().trim().isEmpty()) {
            secondaryData.setJobName(baseData.getJobName2());
            secondaryData.setJob(baseData.getJobName2());
        } else {
            OrganizationTestData jobPosData = setOrganizationHelper().getTestData(OrgSheetName.JOB_POSITION, 0);
            if (jobPosData != null && jobPosData.getJob() != null && !jobPosData.getJob().trim().isEmpty()) {
                secondaryData.setJobName(jobPosData.getJob());
                secondaryData.setJob(jobPosData.getJob());
            }
        }
        return secondaryData;
    }

    @Test(groups = {"regression", "multi-action"}, description = "1. Verify Move Primary Organization and Split Child Organization sequentially")
    public void movePrimaryAndSplitChildTest() {
        logger.info("Starting test: movePrimaryAndSplitChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Move Primary Organization (Sheet MOVE, Action: PRIMARY)
        OrganizationTestData primaryMoveData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "PRIMARY");
        setOrganizationHelper().moveOrganization(primaryMoveData);
        setOrganizationHelper().fillJobPosition(primaryMoveData, OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primaryMoveData.getNewOrganizationName()),
                "Moved Primary Organization node is not displayed");

        // 2. Eksekusi Aksi 2: Split Child Organization dalam Subtree yang sama (Sheet MOVE, Action: CHILD_SPLIT)
        OrganizationTestData childSplit1 = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "CHILD_SPLIT");
        OrganizationTestData childSplit2 = getSecondarySplitData(childSplit1);

        setOrganizationHelper().splitOrganization(childSplit1, childSplit2);
        setOrganizationHelper().fillJobPositionForSplit(childSplit1, childSplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        // 3. Verifikasi Log Perubahan untuk kedua aksi
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Move Organization", primaryMoveData.getPrimaryOrganization(), primaryMoveData.getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly");
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", childSplit1.getPrimaryOrganization()),
                "Changes Log for Split Organization is not displayed correctly");
    }

    @Test(groups = {"regression", "multi-action"}, description = "2. Verify Move Primary Organization and Merge Child Organization sequentially")
    public void movePrimaryAndMergeChildTest() {
        logger.info("Starting test: movePrimaryAndMergeChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Move Primary Organization (Sheet MOVE, Action: PRIMARY)
        OrganizationTestData primaryMoveData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "PRIMARY");
        setOrganizationHelper().moveOrganization(primaryMoveData);
        setOrganizationHelper().fillJobPosition(primaryMoveData, OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);

        // 2. Eksekusi Aksi 2: Merge Child Organization dalam Subtree yang sama (Sheet MOVE, Action: CHILD_MERGE)
        OrganizationTestData childMergeData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "CHILD_MERGE");
        setOrganizationHelper().mergeOrganization(childMergeData);
        setOrganizationHelper().fillJobPosition(childMergeData, OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);

        // 3. Verifikasi Node & Log Perubahan
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childMergeData.getNewOrganizationName()),
                "Merged Child Organization node is not displayed");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Move Organization", primaryMoveData.getPrimaryOrganization(), primaryMoveData.getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly");
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Merge Organization", childMergeData.getPrimaryOrganization(), childMergeData.getMergeOrganizationWith()),
                "Changes Log for Merge Organization is not displayed correctly");
    }

    @Test(groups = {"regression", "multi-action"}, description = "3. Verify Move Primary Organization and Rename Child Organization sequentially")
    public void movePrimaryAndRenameChildTest() {
        logger.info("Starting test: movePrimaryAndRenameChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Move Primary Organization (Sheet MOVE, Action: PRIMARY)
        OrganizationTestData primaryMoveData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "PRIMARY");
        setOrganizationHelper().moveOrganization(primaryMoveData);
        setOrganizationHelper().fillJobPosition(primaryMoveData, OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);

        // 2. Eksekusi Aksi 2: Rename Child Organization dalam Subtree yang sama (Sheet MOVE, Action: CHILD_RENAME)
        OrganizationTestData childRenameData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "CHILD_RENAME");
        setOrganizationHelper().renameOrganization(childRenameData);
        setOrganizationHelper().fillJobPosition(childRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        // 3. Verifikasi Node & Log Perubahan
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childRenameData.getNewOrganizationName()),
                "Renamed Child Organization node is not displayed");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Move Organization", primaryMoveData.getPrimaryOrganization(), primaryMoveData.getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly");
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", childRenameData.getPrimaryOrganization(), childRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly");
    }

    @Test(groups = {"regression", "multi-action"}, description = "4. Verify Rename Primary Organization and Merge Child Organization sequentially")
    public void renamePrimaryAndMergeChildTest() {
        logger.info("Starting test: renamePrimaryAndMergeChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Rename Primary Organization (Sheet RENAME, Action: PRIMARY)
        OrganizationTestData primaryRenameData = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "PRIMARY");
        setOrganizationHelper().renameOrganization(primaryRenameData);
        setOrganizationHelper().fillJobPosition(primaryRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        // 2. Eksekusi Aksi 2: Merge Child Organization dalam Subtree yang sama (Sheet RENAME, Action: CHILD_MERGE)
        OrganizationTestData childMergeData = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "CHILD_MERGE");
        setOrganizationHelper().mergeOrganization(childMergeData);
        setOrganizationHelper().fillJobPosition(childMergeData, OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);

        // 3. Verifikasi Node & Log Perubahan
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childMergeData.getNewOrganizationName()),
                "Merged Child Organization node is not displayed");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", primaryRenameData.getPrimaryOrganization(), primaryRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly");
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Merge Organization", childMergeData.getPrimaryOrganization(), childMergeData.getMergeOrganizationWith()),
                "Changes Log for Merge Organization is not displayed correctly");
    }

    @Test(groups = {"regression", "multi-action"}, description = "5. Verify Rename Primary Organization and Split Child Organization sequentially")
    public void renamePrimaryAndSplitChildTest() {
        logger.info("Starting test: renamePrimaryAndSplitChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Rename Primary Organization (Sheet RENAME, Action: PRIMARY)
        OrganizationTestData primaryRenameData = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "PRIMARY");
        setOrganizationHelper().renameOrganization(primaryRenameData);
        setOrganizationHelper().fillJobPosition(primaryRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        // 2. Eksekusi Aksi 2: Split Child Organization dalam Subtree yang sama (Sheet RENAME, Action: CHILD_SPLIT)
        OrganizationTestData childSplit1 = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "CHILD_SPLIT");
        OrganizationTestData childSplit2 = getSecondarySplitData(childSplit1);

        setOrganizationHelper().splitOrganization(childSplit1, childSplit2);
        setOrganizationHelper().fillJobPositionForSplit(childSplit1, childSplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        // 3. Verifikasi Log Perubahan
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", primaryRenameData.getPrimaryOrganization(), primaryRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly");
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", childSplit1.getPrimaryOrganization()),
                "Changes Log for Split Organization is not displayed correctly");
    }

    @Test(groups = {"regression", "multi-action"}, description = "6. Verify Split Primary Organization and Split Child Organization sequentially")
    public void splitPrimaryAndSplitChildTest() {
        logger.info("Starting test: splitPrimaryAndSplitChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Split Primary Organization (Sheet SPLIT, Action: PRIMARY)
        OrganizationTestData primarySplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "PRIMARY");
        OrganizationTestData primarySplit2 = getSecondarySplitData(primarySplit1);

        setOrganizationHelper().splitOrganization(primarySplit1, primarySplit2);
        setOrganizationHelper().fillJobPositionForSplit(primarySplit1, primarySplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        // 2. Eksekusi Aksi 2: Split Child Organization dalam Subtree yang sama (Sheet SPLIT, Action: CHILD_SPLIT)
        OrganizationTestData childSplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "CHILD_SPLIT");
        OrganizationTestData childSplit2 = getSecondarySplitData(childSplit1);

        setOrganizationHelper().splitOrganization(childSplit1, childSplit2);
        setOrganizationHelper().fillJobPositionForSplit(childSplit1, childSplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        // 3. Verifikasi Log Perubahan
        //TODO: cek kenapa hanya ada 1 validasi?
        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", primarySplit1.getPrimaryOrganization()),
                "Changes Log for Primary Split Organization is not displayed correctly");
    }

    @Test(groups = {"regression", "multi-action"}, description = "7. Verify Split Primary Organization and Rename Child Organization sequentially")
    public void splitPrimaryAndRenameChildTest() {
        logger.info("Starting test: splitPrimaryAndRenameChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Split Primary Organization (Sheet SPLIT, Action: PRIMARY)
        OrganizationTestData primarySplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "PRIMARY");
        OrganizationTestData primarySplit2 = getSecondarySplitData(primarySplit1);

        setOrganizationHelper().splitOrganization(primarySplit1, primarySplit2);
        setOrganizationHelper().fillJobPositionForSplit(primarySplit1, primarySplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        // 2. Eksekusi Aksi 2: Rename Child Organization dalam Subtree yang sama (Sheet SPLIT, Action: CHILD_RENAME)
        OrganizationTestData childRenameData = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "CHILD_RENAME");
        setOrganizationHelper().renameOrganization(childRenameData);
        setOrganizationHelper().fillJobPosition(childRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        // 3. Verifikasi Node & Log Perubahan
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childRenameData.getNewOrganizationName()),
                "Renamed Child Organization node is not displayed");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", primarySplit1.getPrimaryOrganization()),
                "Changes Log for Split Organization is not displayed correctly");
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", childRenameData.getPrimaryOrganization(), childRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly");
    }

    @Test(groups = {"regression", "multi-action"}, description = "8. Verify Split Primary Organization and Merge Child Organization sequentially")
    public void splitPrimaryAndMergeChildTest() {
        logger.info("Starting test: splitPrimaryAndMergeChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        // 1. Eksekusi Aksi 1: Split Primary Organization (Sheet SPLIT, Action: PRIMARY)
        OrganizationTestData primarySplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "PRIMARY");
        OrganizationTestData primarySplit2 = getSecondarySplitData(primarySplit1);

        setOrganizationHelper().splitOrganization(primarySplit1, primarySplit2);
        setOrganizationHelper().fillJobPositionForSplit(primarySplit1, primarySplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        // 2. Eksekusi Aksi 2: Merge Child Organization dalam Subtree yang sama (Sheet SPLIT, Action: CHILD_MERGE)
        OrganizationTestData childMergeData = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "CHILD_MERGE");
        setOrganizationHelper().mergeOrganization(childMergeData);
        setOrganizationHelper().fillJobPosition(childMergeData, OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);

        // 3. Verifikasi Node & Log Perubahan
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childMergeData.getNewOrganizationName()),
                "Merged Child Organization node is not displayed");

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", primarySplit1.getPrimaryOrganization()),
                "Changes Log for Split Organization is not displayed correctly");
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Merge Organization", childMergeData.getPrimaryOrganization(), childMergeData.getMergeOrganizationWith()),
                "Changes Log for Merge Organization is not displayed correctly");
    }
}
