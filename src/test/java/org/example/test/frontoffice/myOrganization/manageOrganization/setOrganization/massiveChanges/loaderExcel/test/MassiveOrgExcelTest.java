package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.test;

import org.example.test.frontoffice.myOrganization.manageOrganization.ManageOrganizationBaseTest;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.config.MassiveTestConfig;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.reader.Flow1ExcelExportReader;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.model.MassiveOrgModel;
import org.example.utils.CustomAssert;
import org.testng.annotations.Test;

import java.util.List;

public class MassiveOrgExcelTest extends ManageOrganizationBaseTest {

    // === Configurable Execution & Environment Parameter Variables ===
    private String customExcelPath = MassiveTestConfig.GENERATED_OUTPUT_EXCEL_PATH;
    private boolean autoPopulateProceed = MassiveTestConfig.AUTO_POPULATE_PROCEED;
    private int targetOrgCount = MassiveTestConfig.TARGET_ORG_COUNT;
    private String d0Organization = MassiveTestConfig.D0_ORGANIZATION;

    @Test(groups = {"massive", "excel", "regression"}, description = "Verify adding massive organizations dynamically read from Excel file")
    public void massiveAddOrganizationsFromExcelTest() {
        logger.info("=== Starting Test: massiveAddOrganizationsFromExcelTest ===");
        logger.info("Configuration: Excel Path [{}], autoPopulateProceed [{}], targetOrgCount [{}]",
                customExcelPath, autoPopulateProceed, targetOrgCount);

        // 1. Read structured organization data from Excel (dynamically parsed starting from Level D1)
        List<MassiveOrgModel> orgList = Flow1ExcelExportReader.readOrgsFromExcel(
                customExcelPath,
                autoPopulateProceed,
                targetOrgCount
        );

        CustomAssert.assertFalse(orgList.isEmpty(), "Parsed organization list from Excel must not be empty");
        logger.info("Successfully read {} organizations from Excel for creation pipeline:", orgList.size());

        for (int i = 0; i < orgList.size(); i++) {
            MassiveOrgModel org = orgList.get(i);
            logger.info(" [{}] Level: {}, Org: '{}', Parent Org: '{}', Position: '{}', Parent Pos: '{}'",
                    i + 1, org.getLevel(), org.getOrgName(), org.getParentOrgName(),
                    org.getPositionName(), org.getParentPositionName());
        }

        // 2. Navigate to Set Organization Canvas (Flow 1)
        manageOrganizationHelper().goToFlow1SetOrganization(d0Organization);

        // 3. Sequentially create each organization node on the canvas
        for (int i = 0; i < orgList.size(); i++) {
            MassiveOrgModel org = orgList.get(i);
            logger.info(">>> [{}/{}] Executing Add Organization: Level [{}] - '{}' under Parent '{}' <<<",
                    i + 1, orgList.size(), org.getLevel(), org.getOrgName(), org.getParentOrgName());

            setOrganizationHelper().addMassiveOrganizationNode(org);

            CustomAssert.assertTrue(
                    setOrganizationHelper().isNodeOrganizationDisplayed(org.getOrgName()),
                    "Node organization [" + org.getOrgName() + "] is not displayed on tree canvas after creation"
            );
            logger.info("Node [{}] verified on canvas successfully", org.getOrgName());
        }

        // 4. Verification: Open Changes Log and assert audit trail
        logger.info("=== Step: Verifying Changes Log for Excel Organization Additions ===");
        setOrganizationHelper().clickChangeLogButton();

        for (MassiveOrgModel org : orgList) {
            boolean isLogged = setOrganizationHelper().verifyOrganizationChangesLog(
                    "New Organization",
                    org.getOrgName(),
                    org.getParentOrgName()
            );
            CustomAssert.assertTrue(isLogged, "Changes Log for [" + org.getOrgName() + "] under [" + org.getParentOrgName() + "] is not displayed correctly");
        }

        logger.info("=== Finished Test: massiveAddOrganizationsFromExcelTest Successfully ===");
    }

    // === Parameter Setters for Data-Driven / Suite Overrides ===
    public void setCustomExcelPath(String path) { this.customExcelPath = path; }
    public void setAutoPopulateProceed(boolean flag) { this.autoPopulateProceed = flag; }
    public void setTargetOrgCount(int count) { this.targetOrgCount = count; }
}
