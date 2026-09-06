package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.enums.frontoffice.manageOrganization.ConfirmAction;
import org.example.enums.frontoffice.manageOrganization.OrgMenuAction;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.example.utils.CustomSoftAssert;
import org.testng.annotations.Test;

public class MultiActionOrganizationTest extends ManageOrganizationBaseTest {

    /**
     * Helper method internal untuk membuat data Organization 2 pada aksi Split.
     * Mengambil JOB_NAME_2 langsung dari data baris Excel terkait, atau fallback ke JOB_POSITION.
     */
    private OrganizationTestData getSecondarySplitData(OrganizationTestData baseData) {
        return baseData.createSecondarySplitData();
    }

    @Test(groups = {"regression", "multi-action"}, description = "1. Verify Move Primary Organization and Split Child Organization sequentially")
    public void movePrimaryAndSplitChildTest() {
        logger.info("Starting test: movePrimaryAndSplitChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primaryMoveData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "PRIMARY");
        setOrganizationHelper().moveOrganization(primaryMoveData);
        setOrganizationHelper().fillJobPosition(primaryMoveData, OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primaryMoveData.getNewOrganizationName()),
                "Moved Primary Organization node is not displayed in tree: " + primaryMoveData.getNewOrganizationName());

        OrganizationTestData childSplit1 = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "CHILD_SPLIT");
        OrganizationTestData childSplit2 = getSecondarySplitData(childSplit1);

        setOrganizationHelper().splitOrganization(childSplit1, childSplit2);
        setOrganizationHelper().fillJobPositionForSplit(childSplit1, childSplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childSplit1.getPrimaryOrganization()),
                "Original organization node is still displayed in tree after split action: " + childSplit1.getPrimaryOrganization());

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childSplit1.getNewOrganizationName1()),
                "Child Split Organization 1 node is not displayed in tree: " + childSplit1.getNewOrganizationName1());

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childSplit2.getNewOrganizationName2()),
                "Child Split Organization 2 node is not displayed in tree: " + childSplit2.getNewOrganizationName2());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Move Organization", primaryMoveData.getPrimaryOrganization(), primaryMoveData.getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly: " + primaryMoveData.getPrimaryOrganization() + " -> " + primaryMoveData.getNewOrganizationName());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", childSplit1.getPrimaryOrganization()),
                "Changes Log for Split Organization is not displayed correctly: " + childSplit1.getPrimaryOrganization());
        softAssert.assertAll();
    }

    @Test(groups = {"regression", "multi-action"}, description = "2. Verify Move Primary Organization and Merge Child Organization sequentially")
    public void movePrimaryAndMergeChildTest() {
        logger.info("Starting test: movePrimaryAndMergeChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primaryMoveData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "PRIMARY");
        setOrganizationHelper().moveOrganization(primaryMoveData);
        setOrganizationHelper().fillJobPosition(primaryMoveData, OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primaryMoveData.getNewOrganizationName()),
                "Moved Primary Organization node is not displayed in tree: " + primaryMoveData.getNewOrganizationName());

        OrganizationTestData childMergeData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "CHILD_MERGE");
        setOrganizationHelper().mergeOrganization(childMergeData);
        setOrganizationHelper().fillJobPosition(childMergeData, OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childMergeData.getPrimaryOrganization()),
                "Source merge organization node is still displayed in tree after merge action: " + childMergeData.getPrimaryOrganization());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childMergeData.getMergeOrganizationWith()),
                "Secondary merge organization node is still displayed in tree after merge action: " + childMergeData.getMergeOrganizationWith());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childMergeData.getNewOrganizationName()),
                "New merged organization node is not displayed in tree: " + childMergeData.getNewOrganizationName());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Move Organization", primaryMoveData.getPrimaryOrganization(), primaryMoveData.getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly: " + primaryMoveData.getPrimaryOrganization() + " -> " + primaryMoveData.getNewOrganizationName());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Merge Organization", childMergeData.getPrimaryOrganization(), childMergeData.getMergeOrganizationWith()),
                "Changes Log for Merge Organization is not displayed correctly: " + childMergeData.getPrimaryOrganization());

        softAssert.assertAll();
    }

    @Test(groups = {"regression", "multi-action"}, description = "3. Verify Move Primary Organization and Rename Child Organization sequentially")
    public void movePrimaryAndRenameChildTest() {
        logger.info("Starting test: movePrimaryAndRenameChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primaryMoveData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "PRIMARY");
        setOrganizationHelper().moveOrganization(primaryMoveData);
        setOrganizationHelper().fillJobPosition(primaryMoveData, OrgMenuAction.MOVE);
        setOrganizationHelper().confirmAction(ConfirmAction.MOVE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primaryMoveData.getNewOrganizationName()),
                "Moved Primary Organization node is not displayed in tree: " + primaryMoveData.getNewOrganizationName());

        OrganizationTestData childRenameData = setOrganizationHelper().getTestData(OrgSheetName.MOVE, "CHILD_RENAME");
        setOrganizationHelper().renameOrganization(childRenameData);
        setOrganizationHelper().fillJobPosition(childRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childRenameData.getPrimaryOrganization()),
                "Original organization node is still displayed in tree after rename action: " + childRenameData.getPrimaryOrganization());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childRenameData.getNewOrganizationName()),
                "Renamed organization node is not displayed in tree: " + childRenameData.getNewOrganizationName());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Move Organization", primaryMoveData.getPrimaryOrganization(), primaryMoveData.getNewOrganizationName()),
                "Changes Log for Move Organization is not displayed correctly: " + primaryMoveData.getPrimaryOrganization() + " -> " + primaryMoveData.getNewOrganizationName());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", childRenameData.getPrimaryOrganization(), childRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly: " + childRenameData.getPrimaryOrganization() + " -> " + childRenameData.getNewOrganizationName());
        softAssert.assertAll();
    }

    @Test(groups = {"regression", "multi-action"}, description = "4. Verify Rename Primary Organization and Merge Child Organization sequentially")
    public void renamePrimaryAndMergeChildTest() {
        logger.info("Starting test: renamePrimaryAndMergeChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primaryRenameData = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "PRIMARY");
        setOrganizationHelper().renameOrganization(primaryRenameData);
        setOrganizationHelper().fillJobPosition(primaryRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primaryRenameData.getNewOrganizationName()),
                "Renamed Primary Organization node is not displayed in tree: " + primaryRenameData.getNewOrganizationName());

        OrganizationTestData childMergeData = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "CHILD_MERGE");
        setOrganizationHelper().mergeOrganization(childMergeData);
        setOrganizationHelper().fillJobPosition(childMergeData, OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childMergeData.getPrimaryOrganization()),
                "Source merge organization node is still displayed in tree after merge action: " + childMergeData.getPrimaryOrganization());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childMergeData.getMergeOrganizationWith()),
                "Secondary merge organization node is still displayed in tree after merge action: " + childMergeData.getMergeOrganizationWith());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childMergeData.getNewOrganizationName()),
                "New merged organization node is not displayed in tree: " + childMergeData.getNewOrganizationName());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", primaryRenameData.getPrimaryOrganization(), primaryRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly: " + primaryRenameData.getPrimaryOrganization() + " -> " + primaryRenameData.getNewOrganizationName());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Merge Organization", childMergeData.getPrimaryOrganization(), childMergeData.getMergeOrganizationWith()),
                "Changes Log for Merge Organization is not displayed correctly: " + childMergeData.getPrimaryOrganization());
        softAssert.assertAll();
    }

    @Test(groups = {"regression", "multi-action"}, description = "5. Verify Rename Primary Organization and Split Child Organization sequentially")
    public void renamePrimaryAndSplitChildTest() {
        logger.info("Starting test: renamePrimaryAndSplitChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primaryRenameData = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "PRIMARY");
        setOrganizationHelper().renameOrganization(primaryRenameData);
        setOrganizationHelper().fillJobPosition(primaryRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primaryRenameData.getNewOrganizationName()),
                "Renamed Primary Organization node is not displayed in tree: " + primaryRenameData.getNewOrganizationName());

        OrganizationTestData childSplit1 = setOrganizationHelper().getTestData(OrgSheetName.RENAME, "CHILD_SPLIT");
        OrganizationTestData childSplit2 = getSecondarySplitData(childSplit1);

        setOrganizationHelper().splitOrganization(childSplit1, childSplit2);
        setOrganizationHelper().fillJobPositionForSplit(childSplit1, childSplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childSplit1.getPrimaryOrganization()),
                "Original organization node is still displayed in tree after split action: " + childSplit1.getPrimaryOrganization());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childSplit1.getNewOrganizationName1()),
                "Child Split Organization 1 node is not displayed in tree: " + childSplit1.getNewOrganizationName1());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childSplit2.getNewOrganizationName2()),
                "Child Split Organization 2 node is not displayed in tree: " + childSplit2.getNewOrganizationName2());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", primaryRenameData.getPrimaryOrganization(), primaryRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly: " + primaryRenameData.getPrimaryOrganization() + " -> " + primaryRenameData.getNewOrganizationName());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", childSplit1.getPrimaryOrganization()),
                "Changes Log for Split Organization is not displayed correctly: " + childSplit1.getPrimaryOrganization());
        softAssert.assertAll();
    }

    @Test(groups = {"regression", "multi-action"}, description = "6. Verify Split Primary Organization and Split Child Organization sequentially")
    public void splitPrimaryAndSplitChildTest() {
        logger.info("Starting test: splitPrimaryAndSplitChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primarySplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "PRIMARY");
        OrganizationTestData primarySplit2 = getSecondarySplitData(primarySplit1);

        setOrganizationHelper().splitOrganization(primarySplit1, primarySplit2);
        setOrganizationHelper().fillJobPositionForSplit(primarySplit1, primarySplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primarySplit1.getNewOrganizationName1()),
                "Primary Split organization 1 node is not displayed in tree: " + primarySplit1.getNewOrganizationName1());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primarySplit2.getNewOrganizationName2()),
                "Primary Split organization 2 node is not displayed in tree: " + primarySplit2.getNewOrganizationName2());

        OrganizationTestData childSplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "CHILD_SPLIT");
        OrganizationTestData childSplit2 = getSecondarySplitData(childSplit1);

        setOrganizationHelper().splitOrganization(childSplit1, childSplit2);
        setOrganizationHelper().fillJobPositionForSplit(childSplit1, childSplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childSplit1.getPrimaryOrganization()),
                "Original organization node is still displayed in tree after split action: " + childSplit1.getPrimaryOrganization());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childSplit1.getNewOrganizationName1()),
                "Child Split organization 1 node is not displayed in tree: " + childSplit1.getNewOrganizationName1());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childSplit2.getNewOrganizationName2()),
                "Child Split organization 2 node is not displayed in tree: " + childSplit2.getNewOrganizationName2());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", primarySplit1.getPrimaryOrganization()),
                "Changes Log for Primary Split Organization is not displayed correctly: " + primarySplit1.getPrimaryOrganization());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", childSplit1.getPrimaryOrganization()),
                "Changes Log for Child Split Organization is not displayed correctly: " + childSplit1.getPrimaryOrganization());
        softAssert.assertAll();
    }

    @Test(groups = {"regression", "multi-action"}, description = "7. Verify Split Primary Organization and Rename Child Organization sequentially")
    public void splitPrimaryAndRenameChildTest() {
        logger.info("Starting test: splitPrimaryAndRenameChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primarySplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "PRIMARY");
        OrganizationTestData primarySplit2 = getSecondarySplitData(primarySplit1);

        setOrganizationHelper().splitOrganization(primarySplit1, primarySplit2);
        setOrganizationHelper().fillJobPositionForSplit(primarySplit1, primarySplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primarySplit1.getNewOrganizationName1()),
                "Primary Split organization 1 node is not displayed in tree: " + primarySplit1.getNewOrganizationName1());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primarySplit2.getNewOrganizationName2()),
                "Primary Split organization 2 node is not displayed in tree: " + primarySplit2.getNewOrganizationName2());

        OrganizationTestData childRenameData = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "CHILD_RENAME");
        setOrganizationHelper().renameOrganization(childRenameData);
        setOrganizationHelper().fillJobPosition(childRenameData, OrgMenuAction.RENAME);
        setOrganizationHelper().confirmAction(ConfirmAction.RENAME);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childRenameData.getPrimaryOrganization()),
                "Original organization node is still displayed in tree after rename action: " + childRenameData.getPrimaryOrganization());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childRenameData.getNewOrganizationName()),
                "Renamed organization node is not displayed in tree: " + childRenameData.getNewOrganizationName());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", primarySplit1.getPrimaryOrganization()),
                "Changes Log for Primary Split Organization is not displayed correctly: " + primarySplit1.getPrimaryOrganization());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Rename Organization", childRenameData.getPrimaryOrganization(), childRenameData.getNewOrganizationName()),
                "Changes Log for Rename Organization is not displayed correctly: " + childRenameData.getPrimaryOrganization() + " -> " + childRenameData.getNewOrganizationName());
        softAssert.assertAll();
    }

    @Test(groups = {"regression", "multi-action"}, description = "8. Verify Split Primary Organization and Merge Child Organization sequentially")
    public void splitPrimaryAndMergeChildTest() {
        logger.info("Starting test: splitPrimaryAndMergeChildTest");
        manageOrganizationHelper().goToFlow1SetOrganization();

        OrganizationTestData primarySplit1 = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "PRIMARY");
        OrganizationTestData primarySplit2 = getSecondarySplitData(primarySplit1);

        setOrganizationHelper().splitOrganization(primarySplit1, primarySplit2);
        setOrganizationHelper().fillJobPositionForSplit(primarySplit1, primarySplit2);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primarySplit1.getNewOrganizationName1()),
                "Primary Split organization 1 node is not displayed in tree: " + primarySplit1.getNewOrganizationName1());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(primarySplit2.getNewOrganizationName2()),
                "Primary Split organization 2 node is not displayed in tree: " + primarySplit2.getNewOrganizationName2());

        OrganizationTestData childMergeData = setOrganizationHelper().getTestData(OrgSheetName.SPLIT, "CHILD_MERGE");
        setOrganizationHelper().mergeOrganization(childMergeData);
        setOrganizationHelper().fillJobPosition(childMergeData, OrgMenuAction.MERGE);
        setOrganizationHelper().confirmAction(ConfirmAction.MERGE);

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childMergeData.getPrimaryOrganization()),
                "Source merge organization node is still displayed in tree after merge action: " + childMergeData.getPrimaryOrganization());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationInvisible(childMergeData.getMergeOrganizationWith()),
                "Secondary merge organization node is still displayed in tree after merge action: " + childMergeData.getMergeOrganizationWith());
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(childMergeData.getNewOrganizationName()),
                "New merged organization node is not displayed in tree: " + childMergeData.getNewOrganizationName());

        setOrganizationHelper().clickChangeLogButton();
        CustomSoftAssert softAssert = new CustomSoftAssert();
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Split Organization", primarySplit1.getPrimaryOrganization()),
                "Changes Log for Primary Split Organization is not displayed correctly: " + primarySplit1.getPrimaryOrganization());
        softAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog(
                "Merge Organization", childMergeData.getPrimaryOrganization(), childMergeData.getMergeOrganizationWith()),
                "Changes Log for Merge Organization is not displayed correctly: " + childMergeData.getPrimaryOrganization());
        softAssert.assertAll();
    }
}
