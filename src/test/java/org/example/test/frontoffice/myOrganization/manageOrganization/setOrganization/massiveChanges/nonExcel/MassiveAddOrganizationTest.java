package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.nonExcel;

import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.config.MassiveTestConfig;
import org.example.model.frontoffice.MassiveOrgModel;
import org.example.utils.CustomAssert;
import org.example.utils.CustomSoftAssert;
import org.testng.annotations.Test;

import java.util.List;

public class MassiveAddOrganizationTest extends ManageOrganizationBaseTest {

    // Target D0 Baseline Variables
    private String directorateOrg = MassiveTestConfig.D0_ORGANIZATION;
    private String directoratePrimaryPosition = MassiveTestConfig.D0_PRIMARY_POSITION;
    private String directorateSecondaryPosition = "Secretary Chief of - Corporate Assurance";

    // Form Dropdown Selection Variables
    private String positionNamingType = MassiveTestConfig.POSITION_NAMING_TYPE;

    // Configurable Control Variables
    private boolean autoPopulateProceed = MassiveTestConfig.AUTO_POPULATE_PROCEED;
    private int targetOrgCount = MassiveTestConfig.TARGET_ORG_COUNT; // Default 15 orgs for non-excel flow, can be set to 5, 10, 15, 30

    @Test(groups = {"massive", "regression"}, description = "Verify adding organizations hierarchically from Level D1 to D6")
    public void massiveAdd15OrganizationsTest() {
        logger.info("=== Starting Test: massiveAdd15OrganizationsTest (D1 -> D6) ===");
        logger.info("Configuration: D0 Org [{}], D0 Primary Pos [{}], Naming Type [{}], autoPopulateProceed [{}], targetOrgCount [{}]",
                directorateOrg, directoratePrimaryPosition, positionNamingType, autoPopulateProceed, targetOrgCount);

        // 1. Generate structured hierarchical organizations (D1 to D6 pyramid topology)
        List<MassiveOrgModel> orgList = MassiveOrgDataFactory.generateOrganizations(
                autoPopulateProceed,
                directorateOrg,
                directoratePrimaryPosition,
                positionNamingType,
                targetOrgCount
        );

        logger.info("Generated {} organizations for creation pipeline:", orgList.size());
        for (int i = 0; i < orgList.size(); i++) {
            MassiveOrgModel org = orgList.get(i);
            logger.info(" [{}] Level: {}, Org: '{}', Parent Org: '{}', Position: '{}', Parent Pos: '{}'",
                    i + 1, org.getLevel(), org.getOrgName(), org.getParentOrgName(),
                    org.getPositionName(), org.getParentPositionName());
        }

        // 2. Navigate to Set Organization Canvas (Flow 1)
        manageOrganizationHelper().goToFlow1SetOrganization(directorateOrg);

        // 3. Sequentially create each organization on the canvas
        for (int i = 0; i < orgList.size(); i++) {
            MassiveOrgModel org = orgList.get(i);
            logger.info(">>> [{}/{}] Executing Add Organization: Level [{}] - '{}' under Parent '{}' <<<",
                    i + 1, orgList.size(), org.getLevel(), org.getOrgName(), org.getParentOrgName());

            setOrganizationHelper().addMassiveOrganizationNode(org);

            CustomAssert.assertTrue(
                    setOrganizationVerifier().isNodeOrganizationDisplayed(org.getOrgName()),
                    "Node organization [" + org.getOrgName() + "] is not displayed on tree canvas after creation"
            );
            logger.info("Node [{}] verified on canvas successfully", org.getOrgName());
        }

        // 4. Verification: Open Changes Log and assert dynamic audit trail
        logger.info("=== Step: Verifying Changes Log for Massive Organization Additions ===");
        setOrganizationHelper().clickChangeLogButton();

        CustomSoftAssert softAssert = new CustomSoftAssert();
        for (MassiveOrgModel org : orgList) {
            boolean isLogged = setOrganizationVerifier().verifyOrganizationChangesLog(
                    "New Organization",
                    org.getOrgName(),
                    org.getParentOrgName()
            );
            softAssert.assertTrue(isLogged, "Changes Log for [" + org.getOrgName() + "] under [" + org.getParentOrgName() + "] is not displayed correctly");
        }
        softAssert.assertAll();
        //        TODO: add step save as draft
        manageOrganizationHelper().saveAsDraft();

        logger.info("=== Finished Test: massiveAdd15OrganizationsTest Successfully ===");
    }

    public int getTargetOrgCount() { return targetOrgCount; }
    public void setTargetOrgCount(int targetOrgCount) { this.targetOrgCount = targetOrgCount; }
}
