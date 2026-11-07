package org.example.test.frontoffice.myOrganization.manageOrganization.data;

import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.model.frontoffice.OrganizationTestData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class ManageOrganizationExcelReaderTest {

    @Test(description = "Verify Excel reader can read data from all sheets in Manage_organization_Data_Test.xlsx")
    public void testReadDataFromAllSheets() {
        OrgSheetName[] sheets = {
            OrgSheetName.SET_FILTER, OrgSheetName.ADD_NEW, OrgSheetName.MERGE, OrgSheetName.SPLIT, 
            OrgSheetName.RENAME, OrgSheetName.REMOVE, OrgSheetName.MOVE, OrgSheetName.JOB_POSITION, OrgSheetName.DUPLICATE
        };

        for (OrgSheetName sheet : sheets) {
            List<OrganizationTestData> dataList = ManageOrganizationExcelReader.readExcelData(sheet);
            Assert.assertNotNull(dataList, "Data list for sheet '" + sheet.getSheetName() + "' should not be null");
            Assert.assertFalse(dataList.isEmpty(), "Data list for sheet '" + sheet.getSheetName() + "' should not be empty");

            OrganizationTestData firstRow = ManageOrganizationExcelReader.getDataByRow(sheet, 0);
            Assert.assertNotNull(firstRow, "First row of sheet '" + sheet.getSheetName() + "' should not be null");
            System.out.printf("Loaded sheet '%s': %d rows. Row 1 -> Action: '%s', PrimaryOrg: '%s', NewOrgName: '%s'%n",
                    sheet.getSheetName(), dataList.size(), firstRow.getAction(), firstRow.getPrimaryOrganization(), firstRow.getNewOrganizationName());
        }

        // Test Semantic Action Lookup
        OrganizationTestData primaryMove = ManageOrganizationExcelReader.getDataByAction(OrgSheetName.MOVE, "PRIMARY");
        Assert.assertEquals(primaryMove.getPrimaryOrganization(), "O223 Network Sales Digitalization D2 ID");

        OrganizationTestData childSplit = ManageOrganizationExcelReader.getDataByAction(OrgSheetName.MOVE, "CHILD_SPLIT");
        Assert.assertEquals(childSplit.getNewOrganizationName1(), "NEW SPLIT O223 Network Sales Specialist D3 ID");

        OrganizationTestData childMerge = ManageOrganizationExcelReader.getDataByAction(OrgSheetName.MOVE, "CHILD_MERGE");
        Assert.assertEquals(childMerge.getMergeOrganizationWith(), "O223 Network Sales Digitalization Support D3 ID");

        OrganizationTestData childRename = ManageOrganizationExcelReader.getDataByAction(OrgSheetName.MOVE, "CHILD_RENAME");
        Assert.assertEquals(childRename.getNewOrganizationName(), "NEW RENAME O223 Network Sales Digitalization D4 ID");
    }
}
