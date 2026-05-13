package org.example.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.config.userLoginStructure.UserloginConfig;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ExcelReader {
    private static final Logger logger = LogManager.getLogger(ExcelReader.class);

    public static List<UserloginConfig> getLoginData(String filePath) {
        List<UserloginConfig> loginDataList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath);
                Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            String lastRole = "";
            String lastUrl = ""; // Tambahkan variabel penampung URL di sini

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null)
                    continue;

                UserloginConfig config = new UserloginConfig();

                // Logika Role (Sudah ada)
                String role = getCellValue(row.getCell(1));
                if (role != null && !role.isEmpty()) {
                    lastRole = role;
                }
                config.setRole(lastRole);

                config.setNip(getCellValue(row.getCell(2)));
                config.setUsername(getCellValue(row.getCell(3)));

                // Logika Password (Sudah ada)
                String password = getCellValue(row.getCell(4));
                if (password == null || password.isEmpty()) {
                    password = TestDataFactory.getPassword();
                }
                config.setPassword(password);

                // --- REVISI LOGIKA URL DI SINI ---
                String excelUrl = getCellValue(row.getCell(5));
                if (excelUrl != null && !excelUrl.isEmpty()) {
                    lastUrl = excelUrl; // Simpan URL jika sel tidak kosong
                }

                if (lastUrl != null && !lastUrl.isEmpty()) {
                    config.setUrlBackOffice(lastUrl);
                    config.setUrlFrontOffice(lastUrl);
                } else {
                    logger.warn("URL is missing for user {} in Excel.", config.getNip());
                }
                // ---------------------------------

                loginDataList.add(config);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return loginDataList;
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
