package org.example.test.frontoffice.myOrganization.manageOrganization.data;

import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class ManageOrganizationExcelReaderTest {

    @Test(description = "Verify Excel reader can read data from all sheets in Manage_organization_Data_Test.xlsx")
    public void testReadDataFromAllSheets() {
        String[] sheets = {
            "SET_FILTER", "ADD_NEW", "MERGE", "SPLIT", 
            "RENAME", "REMOVE", "MOVE", "JOB_POSITION", "DUPLICATE"
        };

        for (String sheet : sheets) {
            List<OrganizationTestData> dataList = ManageOrganizationExcelReader.readExcelData(sheet);
            Assert.assertNotNull(dataList, "Data list for sheet '" + sheet + "' should not be null");
            Assert.assertFalse(dataList.isEmpty(), "Data list for sheet '" + sheet + "' should not be empty");

            OrganizationTestData firstRow = dataList.get(0);
            Assert.assertNotNull(firstRow, "First row of sheet '" + sheet + "' should not be null");
            System.out.printf("Successfully loaded sheet '%s': %d rows found. PrimaryOrg: '%s', NewOrgName: '%s'%n",
                    sheet, dataList.size(), firstRow.getPrimaryOrganization(), firstRow.getNewOrganizationName());
        }
    }
}
