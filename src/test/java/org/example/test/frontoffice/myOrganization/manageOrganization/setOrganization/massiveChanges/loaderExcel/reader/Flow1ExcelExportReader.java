package org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.reader;

import org.apache.poi.ss.usermodel.*;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.config.MassiveTestConfig;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.loaderExcel.utils.UniqueSeedManager;
import org.example.test.frontoffice.myOrganization.manageOrganization.setOrganization.massiveChanges.model.MassiveOrgModel;
import org.example.utils.resource.ResourcePathResolver;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.util.*;

public class Flow1ExcelExportReader {

    private static final String DEFAULT_EXCEL_PATH = MassiveTestConfig.GENERATED_OUTPUT_EXCEL_PATH;
    private static final String FALLBACK_TEMPLATE_PATH = MassiveTestConfig.SOURCE_TEMPLATE_EXCEL_PATH;

    public static List<MassiveOrgModel> read30OrgsFromExcel() {
        return readOrgsFromExcel(DEFAULT_EXCEL_PATH, false, 30);
    }

    public static List<MassiveOrgModel> read30OrgsFromExcel(boolean autoPopulateProceed) {
        return readOrgsFromExcel(DEFAULT_EXCEL_PATH, autoPopulateProceed, 30);
    }

    public static List<MassiveOrgModel> read30OrgsFromExcel(String customPath, boolean autoPopulateProceed) {
        return readOrgsFromExcel(customPath, autoPopulateProceed, 30);
    }

    public static List<MassiveOrgModel> readMultiActionOrgsFromExcel(String customPath, boolean autoPopulateProceed) {
        Path filePath = null;
        if (customPath != null) {
            Path direct = java.nio.file.Paths.get(customPath);
            if (java.nio.file.Files.exists(direct)) {
                filePath = direct;
            }
        }
        if (filePath == null) {
            try {
                filePath = ResourcePathResolver.resolve(customPath, DEFAULT_EXCEL_PATH);
            } catch (Exception e) {
                filePath = ResourcePathResolver.resolve(FALLBACK_TEMPLATE_PATH);
            }
        }

        List<MassiveOrgModel> resultList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet orgSheet = workbook.getSheet("Organization");
            Sheet posSheet = workbook.getSheet("Position");

            if (orgSheet == null || posSheet == null) {
                throw new IllegalStateException("Required sheet 'Organization' or 'Position' not found in Excel file: " + filePath);
            }

            Map<String, PosRowData> posMap = parsePositionSheet(posSheet);

            // Header is at row index 4 (Row 5). Data starts at row index 5.
            for (int r = 5; r <= orgSheet.getLastRowNum(); r++) {
                Row row = orgSheet.getRow(r);
                if (row == null) continue;

                String action = getCellValue(row.getCell(1));
                if (action.isEmpty() || "NO_CHANGE".equalsIgnoreCase(action)) continue;

                String curLevel = getCellValue(row.getCell(2));
                String curOrg = getCellValue(row.getCell(3));
                String curParent = getCellValue(row.getCell(4));
                String curRc = getCellValue(row.getCell(5));

                String newLevel = getCellValue(row.getCell(6));
                String newOrg = getCellValue(row.getCell(7));
                String newParent = getCellValue(row.getCell(8));
                String newRc = getCellValue(row.getCell(9));

                PosRowData posData = posMap.get(newOrg);
                if (posData == null && !curOrg.isEmpty()) {
                    posData = posMap.get(curOrg);
                }

                String jobName = (posData != null) ? posData.jobName : "";
                String posName = (posData != null) ? posData.posName : "";
                String parentPos = (posData != null) ? posData.parentPosName : "";
                String location = (posData != null) ? posData.location : "";
                String fte = (posData != null && !posData.fte.isEmpty()) ? posData.fte : "1";

                MassiveOrgModel model = new MassiveOrgModel(
                        action.toUpperCase(),
                        curLevel,
                        curOrg,
                        curParent,
                        curRc,
                        newLevel,
                        newOrg,
                        newParent,
                        newRc,
                        jobName,
                        MassiveTestConfig.POSITION_NAMING_TYPE,
                        posName,
                        parentPos,
                        location,
                        fte,
                        false
                );

                resultList.add(model);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read multi-action organizations from Excel file [" + filePath + "]: " + e.getMessage(), e);
        }

        return resultList;
    }

    public static List<MassiveOrgModel> readOrgsFromExcel(boolean autoPopulateProceed, int limit) {
        return readOrgsFromExcel(DEFAULT_EXCEL_PATH, autoPopulateProceed, limit);
    }

    public static List<MassiveOrgModel> readOrgsFromExcel(String customPath, boolean autoPopulateProceed, int limit) {
        Path filePath = null;
        if (customPath != null) {
            Path direct = java.nio.file.Paths.get(customPath);
            if (java.nio.file.Files.exists(direct)) {
                filePath = direct;
            }
        }
        if (filePath == null) {
            try {
                filePath = ResourcePathResolver.resolve(customPath, DEFAULT_EXCEL_PATH);
            } catch (Exception e) {
                filePath = ResourcePathResolver.resolve(FALLBACK_TEMPLATE_PATH);
            }
        }

        List<MassiveOrgModel> resultList = new ArrayList<>();

        try (FileInputStream fis = new FileInputStream(filePath.toFile());
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet orgSheet = workbook.getSheet("Organization");
            Sheet posSheet = workbook.getSheet("Position");

            if (orgSheet == null || posSheet == null) {
                throw new IllegalStateException("Required sheet 'Organization' or 'Position' not found in Excel file: " + filePath);
            }

            // Map Position Sheet data by Org Name
            // Key: Org Name -> List of Position Rows (OrgName, JobName, PosName, ParentPosName, Location, FTE)
            Map<String, PosRowData> posMap = parsePositionSheet(posSheet);

            String runSeed = autoPopulateProceed ? UniqueSeedManager.generateUniqueSeed() : "";

            // Header is at row index 4 (Row 5). Data starts at row index 5.
            for (int r = 5; r <= orgSheet.getLastRowNum(); r++) {
                Row row = orgSheet.getRow(r);
                if (row == null) continue;

                String action = getCellValue(row.getCell(1));
                if (!"ADD_NEW".equalsIgnoreCase(action)) continue;

                String level = getCellValue(row.getCell(6));
                String rawOrgName = getCellValue(row.getCell(7));
                String rawParentOrg = getCellValue(row.getCell(8));
                String rcCode = getCellValue(row.getCell(9));

                if (rawOrgName.isEmpty()) continue;

                PosRowData posData = posMap.get(rawOrgName);
                String jobName = (posData != null) ? posData.jobName : "";
                String rawPosName = (posData != null) ? posData.posName : "Pos " + rawOrgName;
                String rawParentPos = (posData != null) ? posData.parentPosName : "";
                String location = (posData != null) ? posData.location : "";
                String fte = (posData != null && !posData.fte.isEmpty()) ? posData.fte : "1";

                String finalOrgName = autoPopulateProceed ? rawOrgName + runSeed : rawOrgName;
                String finalPosName = autoPopulateProceed ? rawPosName + runSeed : rawPosName;
                String finalParentOrg = (autoPopulateProceed && !"D1".equalsIgnoreCase(level)) ? rawParentOrg + runSeed : rawParentOrg;
                String finalParentPos = (autoPopulateProceed && !"D1".equalsIgnoreCase(level)) ? rawParentPos + runSeed : rawParentPos;

                if (autoPopulateProceed) {
                    UniqueSeedManager.registerOrgName(finalOrgName);
                    UniqueSeedManager.registerPosName(finalPosName);
                }

                MassiveOrgModel model = new MassiveOrgModel(
                        level,
                        finalOrgName,
                        finalParentOrg,
                        rcCode,
                        jobName,
                        MassiveTestConfig.POSITION_NAMING_TYPE,
                        finalPosName,
                        finalParentPos,
                        location,
                        fte,
                        false
                );

                resultList.add(model);

                if (limit > 0 && resultList.size() >= limit) {
                    break;
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to read organizations from Excel file [" + filePath + "]: " + e.getMessage(), e);
        }

        return resultList;
    }

    private static Map<String, PosRowData> parsePositionSheet(Sheet posSheet) {
        Map<String, PosRowData> map = new HashMap<>();
        // Header at row index 4. Data starts at row index 5.
        for (int r = 5; r <= posSheet.getLastRowNum(); r++) {
            Row row = posSheet.getRow(r);
            if (row == null) continue;

            String orgName = getCellValue(row.getCell(1));
            String jobName = getCellValue(row.getCell(2));
            String posName = getCellValue(row.getCell(3));
            String parentPosName = getCellValue(row.getCell(4));
            String location = getCellValue(row.getCell(5));
            String fte = getCellValue(row.getCell(6));

            if (!orgName.isEmpty()) {
                map.put(orgName, new PosRowData(orgName, jobName, posName, parentPosName, location, fte));
            }
        }
        return map;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    double val = cell.getNumericCellValue();
                    if (val == (long) val) {
                        return String.valueOf((long) val);
                    }
                    return String.valueOf(val);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return "";
        }
    }

    private static class PosRowData {
        final String orgName;
        final String jobName;
        final String posName;
        final String parentPosName;
        final String location;
        final String fte;

        PosRowData(String orgName, String jobName, String posName, String parentPosName, String location, String fte) {
            this.orgName = orgName;
            this.jobName = jobName;
            this.posName = posName;
            this.parentPosName = parentPosName;
            this.location = location;
            this.fte = fte;
        }
    }
}
