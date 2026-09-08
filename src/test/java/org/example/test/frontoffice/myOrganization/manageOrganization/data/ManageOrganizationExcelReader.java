package org.example.test.frontoffice.myOrganization.manageOrganization.data;

import org.apache.poi.ss.usermodel.*;
import org.example.enums.frontoffice.manageOrganization.OrgSheetName;
import org.example.model.frontoffice.OrganizationTestData;
import org.example.utils.resource.ResourcePathResolver;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ManageOrganizationExcelReader {
    private static final Map<String, List<OrganizationTestData>> DATA_CACHE = new ConcurrentHashMap<>();

    public static OrganizationTestData getSingleData(OrgSheetName sheet) {
        return getDataByRow(sheet, 0);
    }

    public static OrganizationTestData getDataByRow(OrgSheetName sheet, int rowIndex) {
        String sheetName = sheet.getSheetName();
        List<OrganizationTestData> list = DATA_CACHE.computeIfAbsent(sheetName, k -> readExcelData(sheet));
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("No test data found in Excel sheet: " + sheetName);
        }
        if (rowIndex < 0 || rowIndex >= list.size()) {
            throw new IndexOutOfBoundsException("Row index " + rowIndex + " is out of bounds for sheet: " + sheetName + " (total rows: " + list.size() + ")");
        }

        OrganizationTestData rawData = list.get(rowIndex);
        OrganizationTestData clonedData = new OrganizationTestData(rawData);

        if (sheet != OrgSheetName.JOB_POSITION) {
            List<OrganizationTestData> jobPositionList = DATA_CACHE.computeIfAbsent(OrgSheetName.JOB_POSITION.getSheetName(), k -> readExcelData(OrgSheetName.JOB_POSITION));
            if (jobPositionList != null && !jobPositionList.isEmpty()) {
                int jobPosIndex = Math.min(rowIndex, jobPositionList.size() - 1);
                mergeJobPositionDefaults(clonedData, jobPositionList.get(jobPosIndex));
            }
        }

        return clonedData;
    }

    public static OrganizationTestData getDataByAction(OrgSheetName sheet, String actionName) {
        String sheetName = sheet.getSheetName();
        List<OrganizationTestData> list = DATA_CACHE.computeIfAbsent(sheetName, k -> readExcelData(sheet));
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("No test data found in Excel sheet: " + sheetName);
        }
        for (int i = 0; i < list.size(); i++) {
            OrganizationTestData item = list.get(i);
            if (item.getAction() != null && item.getAction().equalsIgnoreCase(actionName.trim())) {
                return getDataByRow(sheet, i);
            }
        }
        throw new IllegalArgumentException("Action '" + actionName + "' not found in sheet: " + sheetName);
    }

    private static void mergeJobPositionDefaults(OrganizationTestData target, OrganizationTestData defaults) {
        if (target.getPositionNamingType() == null || target.getPositionNamingType().trim().isEmpty()) {
            target.setPositionNamingType(defaults.getPositionNamingType());
        }
        if (target.getLocation() == null || target.getLocation().trim().isEmpty()) {
            target.setLocation(defaults.getLocation());
        }
        if ((target.getFteNumbers() == null || target.getFteNumbers().trim().isEmpty()) && defaults.getFte() != null && !defaults.getFte().trim().isEmpty()) {
            target.setFteNumbers(defaults.getFte());
        }
        if ((target.getJobName() == null || target.getJobName().trim().isEmpty()) && defaults.getJob() != null && !defaults.getJob().trim().isEmpty()) {
            target.setJobName(defaults.getJob());
        }
    }
    public static List<OrganizationTestData> readExcelData(OrgSheetName sheet) {
        String sheetName = sheet.getSheetName();
        Path filePath = ResourcePathResolver.resolve(
                "data/frontoffice/manageOrganization/Manage_organization_Data_Test.xlsx",
                "src/test/resources/data/frontoffice/manageOrganization/Manage_organization_Data_Test.xlsx",
                "src/test/resources/data/frontoffice/Manage_organization_Data_Test.xlsx"
        );

        List<OrganizationTestData> dataList = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet excelSheet = workbook.getSheet(sheetName);
            if (excelSheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' tidak ditemukan di file: " + filePath);
            }

            Row headerRow = excelSheet.getRow(0);
            if (headerRow == null) return dataList;

            // Map index kolom berdasarkan nama Header
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerName = formatter.formatCellValue(cell).trim().toUpperCase();
                headerMap.put(headerName, cell.getColumnIndex());
            }

            // Iterasi baris data
            for (int i = 1; i <= excelSheet.getLastRowNum(); i++) {
                Row row = excelSheet.getRow(i);
                if (row == null) continue;

                OrganizationTestData data = new OrganizationTestData();

                // Pemetaan Kolom ACTION
                if (headerMap.containsKey("ACTION")) {
                    data.setAction(getCellValue(row, headerMap.get("ACTION"), formatter));
                } else if (headerMap.containsKey("ACTION_TYPE")) {
                    data.setAction(getCellValue(row, headerMap.get("ACTION_TYPE"), formatter));
                }

                // Pemetaan Kolom Existing & Kolom Baru
                if (headerMap.containsKey("PRIMARY_ORGANIZATION")) {
                    data.setPrimaryOrganization(getCellValue(row, headerMap.get("PRIMARY_ORGANIZATION"), formatter));
                }
                if (headerMap.containsKey("MERGE_ORG_1")) {
                    data.setMergeOrganizationWith(getCellValue(row, headerMap.get("MERGE_ORG_1"), formatter));
                }
                if (headerMap.containsKey("NEW_ORGANIZATION_NAME")) {
                    data.setNewOrganizationName(getCellValue(row, headerMap.get("NEW_ORGANIZATION_NAME"), formatter));
                }
                if (headerMap.containsKey("RC_CODE")) {
                    data.setRcCode(getCellValue(row, headerMap.get("RC_CODE"), formatter));
                }
                if (headerMap.containsKey("LEVEL")) {
                    data.setLevel(getCellValue(row, headerMap.get("LEVEL"), formatter));
                }
                if (headerMap.containsKey("NEW_PARENT_ORGANIZATION")) {
                    data.setNewParentOrganization(getCellValue(row, headerMap.get("NEW_PARENT_ORGANIZATION"), formatter));
                }
                if (headerMap.containsKey("EXPAND_ORGANIZATION")) {
                    data.setExpandOrganization(getCellValue(row, headerMap.get("EXPAND_ORGANIZATION"), formatter));
                }
                if (headerMap.containsKey("NEW_ORGANIZATION_NAME_1")) {
                    data.setNewOrganizationName1(getCellValue(row, headerMap.get("NEW_ORGANIZATION_NAME_1"), formatter));
                }
                if (headerMap.containsKey("NEW_ORGANIZATION_NAME_2")) {
                    data.setNewOrganizationName2(getCellValue(row, headerMap.get("NEW_ORGANIZATION_NAME_2"), formatter));
                }

                // FIELD BARU HASIL UPDATE
                if (headerMap.containsKey("NEW_PARENT_POSITION")) {
                    String val = getCellValue(row, headerMap.get("NEW_PARENT_POSITION"), formatter);
                    data.setNewParentPosition(val);
                    data.setParentPosition(val);
                }
                if (headerMap.containsKey("CHILD_ORGANIZATION")) {
                    data.setChildOrganization(getCellValue(row, headerMap.get("CHILD_ORGANIZATION"), formatter));
                }
                if (headerMap.containsKey("AFFECTED")) {
                    data.setAffected(getCellValue(row, headerMap.get("AFFECTED"), formatter));
                }
                if (headerMap.containsKey("EMPLOYE_NUMBER")) {
                    data.setEmployeeNumber(getCellValue(row, headerMap.get("EMPLOYE_NUMBER"), formatter));
                }
                if (headerMap.containsKey("POSITION_NAME")) {
                    data.setPositionName(getCellValue(row, headerMap.get("POSITION_NAME"), formatter));
                }
                if (headerMap.containsKey("ORGANIZATION_NAME")) {
                    data.setOrganizationName(getCellValue(row, headerMap.get("ORGANIZATION_NAME"), formatter));
                }
                if (headerMap.containsKey("POSITION_NAMING_TYPE")) {
                    data.setPositionNamingType(getCellValue(row, headerMap.get("POSITION_NAMING_TYPE"), formatter));
                }
                if (headerMap.containsKey("LOCATION")) {
                    data.setLocation(getCellValue(row, headerMap.get("LOCATION"), formatter));
                }
                if (headerMap.containsKey("FTE")) {
                    data.setFte(getCellValue(row, headerMap.get("FTE"), formatter));
                }
                if (headerMap.containsKey("JOB")) {
                    String jobVal = getCellValue(row, headerMap.get("JOB"), formatter);
                    data.setJob(jobVal);
                    if (data.getJobName() == null || data.getJobName().trim().isEmpty()) {
                        data.setJobName(jobVal);
                    }
                }
                if (headerMap.containsKey("JOB_NAME")) {
                    // Mendukung kolom 'JOB_NAME' jika ada di sheet utama
                    data.setJobName(getCellValue(row, headerMap.get("JOB_NAME"), formatter));
                }
                if (headerMap.containsKey("JOB_NAME_2")) {
                    data.setJobName2(getCellValue(row, headerMap.get("JOB_NAME_2"), formatter));
                }

                // Field dari SET_FILTER
                if (headerMap.containsKey("DIRECTORATE")) {
                    data.setDirectorate(getCellValue(row, headerMap.get("DIRECTORATE"), formatter));
                }
                if (headerMap.containsKey("SUB_DIRECTORATE")) {
                    data.setSubDirectorate(getCellValue(row, headerMap.get("SUB_DIRECTORATE"), formatter));
                }
                if (headerMap.containsKey("GROUP")) {
                    data.setGroup(getCellValue(row, headerMap.get("GROUP"), formatter));
                }
                if (headerMap.containsKey("DIVISION")) {
                    data.setDivision(getCellValue(row, headerMap.get("DIVISION"), formatter));
                }
                if (headerMap.containsKey("DEPARTEMENT")) {
                    data.setDepartment(getCellValue(row, headerMap.get("DEPARTEMENT"), formatter));
                }

                // Field dari Sheet ASSIGN_EMPLOYEE
                if (headerMap.containsKey("TYPE")) {
                    data.setType(getCellValue(row, headerMap.get("TYPE"), formatter));
                }
                if (headerMap.containsKey("NIP")) {
                    data.setNip(getCellValue(row, headerMap.get("NIP"), formatter));
                }
                if (headerMap.containsKey("CURRENT_ORG_LEVEL")) {
                    data.setCurrentOrgLevel(getCellValue(row, headerMap.get("CURRENT_ORG_LEVEL"), formatter));
                }
                if (headerMap.containsKey("CURRENT_ORG")) {
                    data.setCurrentOrg(getCellValue(row, headerMap.get("CURRENT_ORG"), formatter));
                }
                if (headerMap.containsKey("CURRENT_JOB")) {
                    data.setCurrentJob(getCellValue(row, headerMap.get("CURRENT_JOB"), formatter));
                }
                if (headerMap.containsKey("CURRENT_POSITION")) {
                    data.setCurrentPosition(getCellValue(row, headerMap.get("CURRENT_POSITION"), formatter));
                }
                if (headerMap.containsKey("MOVEMENT_MOBILITY")) {
                    data.setMovementMobility(getCellValue(row, headerMap.get("MOVEMENT_MOBILITY"), formatter));
                }
                if (headerMap.containsKey("MOVEMENT_CATEGORY")) {
                    data.setMovementCategory(getCellValue(row, headerMap.get("MOVEMENT_CATEGORY"), formatter));
                }

                dataList.add(data);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to read Manage Organization data from: " + filePath, e);
        }

        return dataList;
    }

    public static OrganizationTestData getDataByType(OrgSheetName sheet, String typeName) {
        String sheetName = sheet.getSheetName();
        List<OrganizationTestData> list = DATA_CACHE.computeIfAbsent(sheetName, k -> readExcelData(sheet));
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("No test data found in Excel sheet: " + sheetName);
        }
        for (int i = 0; i < list.size(); i++) {
            OrganizationTestData item = list.get(i);
            if (item.getType() != null && item.getType().equalsIgnoreCase(typeName.trim())) {
                return new OrganizationTestData(item);
            }
        }
        throw new IllegalArgumentException("Type '" + typeName + "' not found in sheet: " + sheetName);
    }

    private static String getCellValue(Row row, Integer columnIndex, DataFormatter formatter) {
        if (columnIndex == null) return "";
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
