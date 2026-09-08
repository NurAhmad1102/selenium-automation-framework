package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.nonExcel;

import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.config.MassiveTestConfig;
import org.example.model.frontoffice.MassiveOrgModel;
import org.example.utils.CustomAssert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MassiveOrgDataFactoryTest {

//    UNIT TESTS for MassiveOrgDataFactory non-excel generation of organizations and positions

    private final String directorate = MassiveTestConfig.D0_ORGANIZATION;
    private final String directoratePosition = MassiveTestConfig.D0_PRIMARY_POSITION;
    private final String namingType = MassiveTestConfig.POSITION_NAMING_TYPE;

    @Test(description = "Verify 15 organizations generation topology, non-duplicate names, and parent-child linkages")
    public void testGenerate15OrganizationsWithUniqueProceed() {
        List<MassiveOrgModel> orgs = MassiveOrgDataFactory.generate15Organizations(
                true, directorate, directoratePosition, namingType
        );

        CustomAssert.assertEquals(orgs.size(), 15, "Total organizations generated must be exactly 15");

        Set<String> orgNames = new HashSet<>();
        Set<String> posNames = new HashSet<>();

        for (int i = 0; i < orgs.size(); i++) {
            MassiveOrgModel org = orgs.get(i);

            // Assert uniqueness
            CustomAssert.assertTrue(orgNames.add(org.getOrgName()), "Duplicate organization name detected: " + org.getOrgName());
            CustomAssert.assertTrue(posNames.add(org.getPositionName()), "Duplicate position name detected: " + org.getPositionName());

            // Assert non-empty critical fields
            CustomAssert.assertTrue(org.getRcCode() != null, "RC Code must not be null");
            CustomAssert.assertFalse(org.getRcCode().isEmpty(), "RC Code must not be empty");
            CustomAssert.assertTrue(org.getJobName() != null, "Job Name must not be null");
            CustomAssert.assertTrue(org.getLocation() != null, "Location must not be null");
            CustomAssert.assertEquals(org.getPositionNamingType(), namingType, "Position naming type mismatch");

            // Assert hierarchical links
            if (org.getLevel().equals("D1")) {
                CustomAssert.assertEquals(org.getParentOrgName(), directorate, "D1 Parent Org mismatch");
                CustomAssert.assertEquals(org.getParentPositionName(), directoratePosition, "D1 Parent Position mismatch");
            } else {
                // Must link to one of the previously created orgs & positions
                boolean parentOrgFound = false;
                boolean parentPosFound = false;
                for (int j = 0; j < i; j++) {
                    if (orgs.get(j).getOrgName().equals(org.getParentOrgName())) {
                        parentOrgFound = true;
                    }
                    if (orgs.get(j).getPositionName().equals(org.getParentPositionName()) || org.getParentPositionName().equals(directoratePosition)) {
                        parentPosFound = true;
                    }
                }
                CustomAssert.assertTrue(parentOrgFound, "Parent Org [" + org.getParentOrgName() + "] not found among preceding nodes for node: " + org.getOrgName());
                CustomAssert.assertTrue(parentPosFound, "Parent Position [" + org.getParentPositionName() + "] not found among preceding nodes for node: " + org.getOrgName());
            }
        }

    }

    @Test(description = "Verify configurable limit for Non-Excel dynamic generation (5, 10, 30 orgs)")
    public void testGenerateOrganizationsWithDynamicLimit() {
        List<MassiveOrgModel> orgs5 = MassiveOrgDataFactory.generateOrganizations(true, directorate, directoratePosition, namingType, 5);
        CustomAssert.assertEquals(orgs5.size(), 5, "Non-excel factory should generate 5 orgs when limit is 5");

        List<MassiveOrgModel> orgs10 = MassiveOrgDataFactory.generateOrganizations(true, directorate, directoratePosition, namingType, 10);
        CustomAssert.assertEquals(orgs10.size(), 10, "Non-excel factory should generate 10 orgs when limit is 10");

        List<MassiveOrgModel> orgs30 = MassiveOrgDataFactory.generateOrganizations(true, directorate, directoratePosition, namingType, 30);
        CustomAssert.assertEquals(orgs30.size(), 30, "Non-excel factory should generate 30 orgs when limit is 30");
    }

    @Test(description = "Verify deterministic names when autoPopulateProceed is false")
    public void testGenerate15OrganizationsDeterministic() {
        List<MassiveOrgModel> run1 = MassiveOrgDataFactory.generate15Organizations(false, directorate, directoratePosition, namingType);
        List<MassiveOrgModel> run2 = MassiveOrgDataFactory.generate15Organizations(false, directorate, directoratePosition, namingType);

        CustomAssert.assertEquals(run1.size(), 15, "Run1 total size must be 15");
        CustomAssert.assertEquals(run2.size(), 15, "Run2 total size must be 15");

        for (int i = 0; i < 15; i++) {
            CustomAssert.assertEquals(run1.get(i).getOrgName(), run2.get(i).getOrgName(), "Names must match when autoPopulateProceed=false");
            CustomAssert.assertEquals(run1.get(i).getPositionName(), run2.get(i).getPositionName(), "Position names must match when autoPopulateProceed=false");
        }
    }
}
