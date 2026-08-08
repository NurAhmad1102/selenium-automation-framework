package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization;

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
        this.data.set(setOrganizationHelper().getTestData("SPLIT"));
    }

    @Test(groups = {"smoke"}, description = "Verify that user can split organization with valid data and different job for Organization 2")
    public void splitOrganizationTest() {
        logger.info("Starting test: splitOrganizationTest");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);

        OrganizationTestData jobPositionData = setOrganizationHelper().getTestData("JOB_POSITION");
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
        setOrganizationHelper().confirmAction("Split");

        String org1Name = org1Data.getNewOrganizationName1() != null ? org1Data.getNewOrganizationName1() : org1Data.getNewOrganizationName();
        String org2Name = org2Data.getNewOrganizationName2() != null ? org2Data.getNewOrganizationName2() : org2Data.getNewOrganizationName();

        if (org1Name != null) {
            CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(org1Name),
                    "Split Organization 1 node is not displayed in the organization tree");
        }
        if (org2Name != null) {
            CustomAssert.assertTrue(setOrganizationHelper().isNodeOrganizationDisplayed(org2Name),
                    "Split Organization 2 node is not displayed in the organization tree");
        }
    }

    @Test(groups = {"smoke"}, description = "Verify that validation user can't split organization with same Organization name in Existing Organization")
    public void splitOrganizationWithExistingOrganizationNameTest() {
        logger.info("Starting test: splitOrganizationTest");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);
        org2Data.setNewOrganizationName(duplicateOrganizationName);

        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed");
    }

    @Test(groups = {"smoke"}, description = "Verify that validation user can't split organization with same Organization name in Both New Organization")
    public void splitOrganizationWithSecondOrganizationNameTest() {
        logger.info("Starting test: splitOrganizationTest");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);
        org2Data.setNewOrganizationName(org1Data.getNewOrganizationName1());

        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(duplicateOrganizationMessage), "Inline error message is not displayed");
    }

    @Test(groups = {"smoke"}, description = "Verify that user can split organization with duplicate position")
    public void splitOrganizationTestWithDuplicatePosition() {
        logger.info("Starting test: splitOrganizationTestWithDuplicatePosition");
        manageOrganizationHelper().goToFlow1();

        OrganizationTestData org1Data = data.get();
        OrganizationTestData org2Data = new OrganizationTestData(org1Data);

        setOrganizationHelper().splitOrganization(org1Data, org2Data);
        setOrganizationHelper().fillJobPositionForSplit(org1Data, org2Data, true);
        CustomAssert.assertTrue(setOrganizationHelper().isInlineErrorMessageDisplayed(splitDuplicatePositionInBothOrg), "Inline error message is not displayed");
    }
}
