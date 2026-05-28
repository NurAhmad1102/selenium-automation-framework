package org.example.utils.excelReader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.config.userLoginStructure.UserloginConfig;
import org.example.utils.TestDataFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExcelReader {
    private static final Logger logger = LogManager.getLogger(ExcelReader.class);

    // Sheet Names
    private static final String ACM_SHEET = "ACM";
    private static final String URL_SHEET = "URL";

    // Column Indices for ACM sheet (Role=1, Nip=2, Username=3, Password=4)
    private static final int COL_ROLE = 1;
    private static final int COL_NIP = 2;
    private static final int COL_USERNAME = 3;
    private static final int COL_PASSWORD = 4;

    // Column Indices for URL sheet (Interface=0, URL=1)
    private static final int COL_INTERFACE = 0;
    private static final int COL_URL = 1;

    /**
     * Container for both account data and global URLs.
     */
    public static class LoginData {
        private final List<UserloginConfig> accounts;
        private final Map<String, String> urls;

        public LoginData(List<UserloginConfig> accounts, Map<String, String> urls) {
            this.accounts = accounts;
            this.urls = urls;
        }

        public List<UserloginConfig> getAccounts() {
            return accounts;
        }

        public Map<String, String> getUrls() {
            return urls;
        }
    }

    /**
     * Reads all login data from Excel.
     * @param filePath Path to the Excel file.
     * @return LoginData object containing accounts and URLs.
     */
    public static LoginData readExcel(String filePath) {
        List<UserloginConfig> accounts = new ArrayList<>();
        Map<String, String> urls = new HashMap<>();

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new XSSFWorkbook(fis)) {

            // 1. Read URL sheet
            urls = readUrlSheet(workbook);

            // 2. Read ACM sheet
            Sheet acmSheet = workbook.getSheet(ACM_SHEET);
            if (acmSheet != null) {
                accounts = readAcmSheet(acmSheet, urls);
            } else {
                logger.error("Sheet '{}' not found in Excel: {}", ACM_SHEET, filePath);
            }

        } catch (IOException e) {
            logger.error("Error reading Excel file: {}", filePath, e);
        }

        return new LoginData(accounts, urls);
    }

    /**
     * Backward compatibility method.
     */
    public static List<UserloginConfig> getLoginData(String filePath) {
        return readExcel(filePath).getAccounts();
    }

    private static Map<String, String> readUrlSheet(Workbook workbook) {
        Map<String, String> urlMap = new HashMap<>();
        Sheet sheet = workbook.getSheet(URL_SHEET);
        if (sheet == null) {
            logger.warn("Sheet '{}' not found. URLs might be missing.", URL_SHEET);
            return urlMap;
        }

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            String interfaceType = getCellValue(row.getCell(COL_INTERFACE));
            String url = getCellValue(row.getCell(COL_URL));

            if (interfaceType != null && !interfaceType.isEmpty()) {
                urlMap.put(interfaceType.trim().toLowerCase(), url);
            }
        }
        return urlMap;
    }

    private static List<UserloginConfig> readAcmSheet(Sheet sheet, Map<String, String> urls) {
        List<UserloginConfig> accountList = new ArrayList<>();
        String lastRole = "";

        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            UserloginConfig config = new UserloginConfig();

            // Handle merged/inherited Role
            String role = getCellValue(row.getCell(COL_ROLE));
            if (role != null && !role.isEmpty()) {
                lastRole = role;
            }
            config.setRole(lastRole);

            config.setNip(getCellValue(row.getCell(COL_NIP)));
            config.setUsername(getCellValue(row.getCell(COL_USERNAME)));

            // Handle Password with fallback
            String password = getCellValue(row.getCell(COL_PASSWORD));
            if (password == null || password.isEmpty()) {
                password = TestDataFactory.getPassword();
            }
            config.setPassword(password);

            // Populate URLs into config for backward compatibility
            config.setUrlBackOffice(urls.getOrDefault("backoffice", ""));
            config.setUrlFrontOffice(urls.getOrDefault("frontoffice", ""));

            accountList.add(config);
        }
        return accountList;
    }

    private static String getCellValue(Cell cell) {
        if (cell == null)
            return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    return String.format("%.0f", cell.getNumericCellValue());
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            default:
                return "";
        }
    }
}
