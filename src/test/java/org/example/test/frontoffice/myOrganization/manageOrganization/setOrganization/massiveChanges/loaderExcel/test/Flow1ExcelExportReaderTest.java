package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.test;

import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.reader.Flow1ExcelExportReader;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.utils.UniqueSeedManager;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.model.MassiveOrgModel;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.nonExcel.MassiveOrgDataFactory;
import org.example.utils.CustomAssert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class Flow1ExcelExportReaderTest {

//    UNIT TESTS for Flow1ExcelExportReader and its integration with UniqueSeedManager and MassiveOrgDataFactory
    @BeforeMethod
    public void setUp() {
        UniqueSeedManager.clearRegistry();
    }

    @Test(description = "Verify that Flow1ExcelExportReader can parse 30 organizations from Excel")
    public void testRead30OrgsFromExcelStatic() {
        List<MassiveOrgModel> orgs = Flow1ExcelExportReader.read30OrgsFromExcel(false);

        CustomAssert.assertEquals(orgs.size(), 30, "Total organizations parsed from Excel should be 30");

        // Verify Level D1 (first 2 nodes)
        MassiveOrgModel node1 = orgs.get(0);
        CustomAssert.assertEquals(node1.getLevel(), "D1", "Node 1 level should be D1");
        CustomAssert.assertTrue(node1.getOrgName().startsWith("Transformation Strategy & Governance D1"), "Node 1 org name match");
        CustomAssert.assertFalse(node1.getParentOrgName().isEmpty(), "Node 1 parent org should not be empty");
        CustomAssert.assertFalse(node1.getParentPositionName().isEmpty(), "Node 1 parent position should not be empty");

        MassiveOrgModel node2 = orgs.get(1);
        CustomAssert.assertEquals(node2.getLevel(), "D1", "Node 2 level should be D1");
        CustomAssert.assertTrue(node2.getOrgName().startsWith("Core Technology & Engineering D1"), "Node 2 org name match");

        // Verify Level D6 (last node)
        MassiveOrgModel lastNode = orgs.get(29);
        CustomAssert.assertEquals(lastNode.getLevel(), "D6", "Node 30 level should be D6");
        CustomAssert.assertTrue(lastNode.getOrgName().startsWith("Kubernetes Platform Squad D6"), "Node 30 org name match");
        CustomAssert.assertTrue(lastNode.getParentOrgName().startsWith("Container Operations Sub-Unit D5"), "Node 30 parent org match");
    }

    @Test(description = "Verify configurable limit feature (read 5, 10 orgs)")
    public void testReadOrgsWithLimit() {
        List<MassiveOrgModel> orgs5 = Flow1ExcelExportReader.readOrgsFromExcel(false, 5);
        CustomAssert.assertEquals(orgs5.size(), 5, "Should read exactly 5 organizations when limit is 5");

        List<MassiveOrgModel> orgs10 = Flow1ExcelExportReader.readOrgsFromExcel(false, 10);
        CustomAssert.assertEquals(orgs10.size(), 10, "Should read exactly 10 organizations when limit is 10");
    }

    @Test(description = "Verify synchronized unique names between MassiveOrgDataFactory and Flow1ExcelExportReader via UniqueSeedManager")
    public void testSynchronizedUniquenessBetweenFactoryAndExcelReader() {
        String d0Org = "Corporate Assurance ID";
        String d0Pos = "Chief Audit Executive";
        String namingType = "Area of Responsibility / Job Scope";

        // 1. Factory generates 15 orgs with autoPopulateProceed = true
        List<MassiveOrgModel> factoryOrgs = MassiveOrgDataFactory.generate15Organizations(true, d0Org, d0Pos, namingType);
        CustomAssert.assertEquals(factoryOrgs.size(), 15, "Factory should generate 15 orgs");

        // 2. Excel Reader reads 30 orgs with autoPopulateProceed = true
        List<MassiveOrgModel> excelOrgs = Flow1ExcelExportReader.read30OrgsFromExcel(true);
        CustomAssert.assertEquals(excelOrgs.size(), 30, "Excel reader should parse 30 orgs");

        // 3. Verify zero name collisions between factoryOrgs and excelOrgs
        for (MassiveOrgModel fOrg : factoryOrgs) {
            for (MassiveOrgModel eOrg : excelOrgs) {
                CustomAssert.assertFalse(
                        fOrg.getOrgName().equals(eOrg.getOrgName()),
                        "Factory Org Name [" + fOrg.getOrgName() + "] collided with Excel Org Name"
                );
                CustomAssert.assertFalse(
                        fOrg.getPositionName().equals(eOrg.getPositionName()),
                        "Factory Pos Name [" + fOrg.getPositionName() + "] collided with Excel Pos Name"
                );
            }
        }
    }

    @Test(description = "Verify that Flow1ExcelExportReader can parse MultiAction Excel (3493)")
    public void testReadMultiActionFrom3493Excel() {
        String path3493 = "src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/MultiAction/organization-development-flow1-3493.xlsx";
        List<MassiveOrgModel> actionList = Flow1ExcelExportReader.readMultiActionOrgsFromExcel(path3493, false);

        CustomAssert.assertFalse(actionList.isEmpty(), "Action list from 3493 should not be empty");
        CustomAssert.assertEquals(actionList.size(), 8, "3493 Excel has 8 action rows (REMOVE, RENAME, MERGE x2, SPLIT x2, MOVE, ADD_NEW)");

        // Assert individual actions
        CustomAssert.assertEquals(actionList.get(0).getAction(), "REMOVE", "Row 1 should be REMOVE");
        CustomAssert.assertEquals(actionList.get(1).getAction(), "RENAME", "Row 2 should be RENAME");
        CustomAssert.assertEquals(actionList.get(2).getAction(), "MERGE", "Row 3 should be MERGE");
        CustomAssert.assertEquals(actionList.get(3).getAction(), "MERGE", "Row 4 should be MERGE");
        CustomAssert.assertEquals(actionList.get(4).getAction(), "SPLIT", "Row 5 should be SPLIT");
        CustomAssert.assertEquals(actionList.get(5).getAction(), "MOVE", "Row 6 should be MOVE");
        CustomAssert.assertEquals(actionList.get(6).getAction(), "SPLIT", "Row 7 should be SPLIT");
        CustomAssert.assertEquals(actionList.get(7).getAction(), "ADD_NEW", "Row 8 should be ADD_NEW");
    }

    @Test(description = "Verify that Flow1ExcelExportReader can parse newly generated MultiAction Excel")
    public void testReadNewlyGeneratedMultiActionExcel() {
        String pathGenerated = "src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/generator/organization-development-flow1-multiaction.xlsx";
        List<MassiveOrgModel> actionList = Flow1ExcelExportReader.readMultiActionOrgsFromExcel(pathGenerated, false);

        CustomAssert.assertFalse(actionList.isEmpty(), "Action list from generated multiaction should not be empty");
        CustomAssert.assertEquals(actionList.size(), 8, "Generated MultiAction Excel has 8 action rows");
    }
}
