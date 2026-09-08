package org.example.test.frontoffice.myOrganization.manageOrganization.data;

import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.model.frontoffice.OrganizationTestData;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ManageOrganizationCacheTest {

    @Test(description = "Verify that mutating returned OrganizationTestData does not pollute subsequent reader calls")
    public void testCacheImmutabilityAcrossInvocations() {
        OrganizationTestData initialData = ManageOrganizationExcelReader.getDataByRow(OrgSheetName.ADD_NEW, 0);
        String originalJobName = initialData.getJobName();
        String originalOrgName = initialData.getNewOrganizationName();

        // Perform mutation on the returned object (simulating what a test might do)
        initialData.setJobName("MUTATED_JOB_TEST_NAME");
        initialData.setNewOrganizationName("MUTATED_ORG_TEST_NAME");

        // Retrieve the same row again
        OrganizationTestData freshData = ManageOrganizationExcelReader.getDataByRow(OrgSheetName.ADD_NEW, 0);

        Assert.assertEquals(freshData.getJobName(), originalJobName,
                "Cache was corrupted! Expected original job name but got mutated value.");
        Assert.assertEquals(freshData.getNewOrganizationName(), originalOrgName,
                "Cache was corrupted! Expected original organization name but got mutated value.");
    }
}