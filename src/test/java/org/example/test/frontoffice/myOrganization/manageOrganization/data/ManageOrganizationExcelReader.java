package org.example.test.frontoffice.myOrganization.manageOrganization.data;

import org.apache.poi.ss.usermodel.*;
import org.example.test.frontoffice.myOrganization.manageOrganization.data.model.OrganizationTestData;
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

    public static OrganizationTestData getSingleData(String sheetName) {
        DATA_CACHE.putIfAbsent(sheetName, readExcelData(sheetName));
        List<OrganizationTestData> list = DATA_CACHE.get(sheetName);
        if (list == null || list.isEmpty()) {
            throw new IllegalStateException("No test data found in Excel sheet: " + sheetName);
        }

        OrganizationTestData data = list.get(0);

        if (!"JOB_POSITION".equalsIgnoreCase(sheetName)) {
            DATA_CACHE.putIfAbsent("JOB_POSITION", readExcelData("JOB_POSITION"));
            List<OrganizationTestData> jobPositionList = DATA_CACHE.get("JOB_POSITION");
            if (jobPositionList != null && !jobPositionList.isEmpty()) {
                mergeJobPositionDefaults(data, jobPositionList.get(0));
            }
        }

        return new OrganizationTestData(data);
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
    public static List<OrganizationTestData> readExcelData(String sheetName) {
        Path filePath = ResourcePathResolver.resolve(
                "data/frontoffice/Manage_organization_Data_Test.xlsx",
                "src/test/resources/data/frontoffice/Manage_organization_Data_Test.xlsx",
                "src/test/java/resources/data/frontoffice/Manage_organization_Data_Test.xlsx"
        );

        List<OrganizationTestData> dataList = new ArrayList<>();
        DataFormatter formatter = new DataFormatter();

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' tidak ditemukan di file: " + filePath);
            }

            Row headerRow = sheet.getRow(0);
            if (headerRow == null) return dataList;

            // Map index kolom berdasarkan nama Header
            Map<String, Integer> headerMap = new HashMap<>();
            for (Cell cell : headerRow) {
                String headerName = formatter.formatCellValue(cell).trim().toUpperCase();
                headerMap.put(headerName, cell.getColumnIndex());
            }

            // Iterasi baris data
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                OrganizationTestData data = new OrganizationTestData();

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

                dataList.add(data);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to read Manage Organization data from: " + filePath, e);
        }

        return dataList;
    }

    private static String getCellValue(Row row, Integer columnIndex, DataFormatter formatter) {
        if (columnIndex == null) return "";
        Cell cell = row.getCell(columnIndex);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }
}
