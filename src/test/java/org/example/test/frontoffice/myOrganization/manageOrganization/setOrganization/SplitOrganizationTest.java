package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

import org.example.pageObject.frontoffice.manageOrganization.enums.ConfirmAction;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgSheetName;
import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.example.utils.CustomAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class SplitOrganizationTest extends ManageOrganizationBaseTest {

    private final ThreadLocal<OrganizationTestData> data = new ThreadLocal<>();

    @Override
    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result) {
        super.initManageOrganizationContext(result);
        this.data.set(setOrganizationHelper().getTestData(OrgSheetName.SPLIT));
    }

    @Test(groups = {"smoke"}, description = "Verify that user can split organization with valid data and different job for Organization 2")
    public void splitOrganizationTest() {
        logger.info("Starting test: splitOrganizationTest");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);

        OrganizationTestData jobPositionData = setOrganizationHelper().getTestData(OrgSheetName.JOB_POSITION);
        if (jobPositionData != null) {
            String targetJob = (jobPositionData.getJob() != null && !jobPositionData.getJob().trim().isEmpty())
                    ? jobPositionData.getJob()
                    : jobPositionData.getJobName();
            if (targetJob != null && !targetJob.trim().isEmpty()) {
                org2Data.setJobName(targetJob);
                org2Data.setJob(targetJob);
            }
        }

        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        setOrganizationHelper().fillJobPositionForSplit(org1Data, org2Data);
        setOrganizationHelper().confirmAction(ConfirmAction.SPLIT);

        String org1Name = org1Data.getNewOrganizationName1();
        String org2Name = org2Data.getNewOrganizationName2();

        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(org1Name),
                    "Split Organization 1 node is not displayed in the organization tree");
        CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(org2Name),
                    "Split Organization 2 node is not displayed in the organization tree");

//        ORG 1 verification
        convertNewOrganizationNameForSplit(org1Data, 1);
        setOrganizationHelper().verifyDetailNodeOrganization(data.get());
//        ORG 2 verification
        convertNewOrganizationNameForSplit(org2Data, 2);
        setOrganizationHelper().verifyDetailNodeOrganization(data.get());

        setOrganizationHelper().clickChangeLogButton();
        CustomAssert.assertTrue(setOrganizationHelper().verifyOrganizationChangesLog("Split Organization", org1Data.getPrimaryOrganization(), org1Name, org2Name),
                "Changes Log for Split Organization is not displayed correctly");
    }

    @Test(groups = {"smoke"}, description = "Verify that user cannot split organization with same Organization name in Existing Organization")
    public void splitOrganizationWithExistingOrganizationNameTest() {
        logger.info("Starting test: splitOrganizationTest");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);
        org2Data.setNewOrganizationName2(duplicateOrganizationName);

        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed");
    }

    @Test(groups = {"smoke"}, description = "Verify that user cannot split organization with same Organization name in Both New Organization")
    public void splitOrganizationWithSecondOrganizationNameTest() {
        logger.info("Starting test: splitOrganizationTest");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);
        org2Data.setNewOrganizationName2(org1Data.getNewOrganizationName1());

        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationNameInBothOrg), "Inline error message is not displayed");
    }

    @Test(groups = {"smoke"}, description = "Verify that user cannot split organization with duplicate position")
    public void splitOrganizationTestWithDuplicatePosition() {
        logger.info("Starting test: splitOrganizationTestWithDuplicatePosition");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);

        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        setOrganizationHelper().fillJobPositionForSplit(org1Data, org2Data, true);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(splitDuplicatePositionInBothOrg), "Inline error message is not displayed");
    }

    //to convert the new organization name for split based on the organization number (1 or 2)
    private OrganizationTestData convertNewOrganizationNameForSplit(OrganizationTestData orgData, int OrgNumber) {
        if(OrgNumber == 1) {
            orgData.setNewOrganizationName(orgData.getNewOrganizationName1());
            logger.debug("New Organization 1 Name is: " + orgData.getNewOrganizationName());
        } else if(OrgNumber == 2) {
            orgData.setNewOrganizationName(orgData.getNewOrganizationName2());
            logger.debug("New Organization 2 Name is: " + orgData.getNewOrganizationName());
        } else {
            throw new IllegalArgumentException("Invalid organization number: " + OrgNumber + ", must be 1 or 2");
        }
        return orgData;
    }
}