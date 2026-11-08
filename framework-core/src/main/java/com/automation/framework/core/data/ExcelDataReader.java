package com.automation.framework.core.data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Agnostic Excel sheet data reader that converts rows into maps of column-name to cell-value.
 */
public class ExcelDataReader {

    private static final DataFormatter FORMATTER = new DataFormatter();

    public static List<Map<String, String>> readSheet(Path filePath, String sheetName) {
        try (InputStream is = Files.newInputStream(filePath);
             Workbook workbook = WorkbookFactory.create(is)) {

            Sheet sheet = sheetName != null ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet '" + sheetName + "' not found in " + filePath);
            }

            List<Map<String, String>> rows = new ArrayList<>();
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return rows;
            }

            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(FORMATTER.formatCellValue(cell).trim());
            }

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Map<String, String> rowMap = new LinkedHashMap<>();
                boolean hasContent = false;
                for (int c = 0; c < headers.size(); c++) {
                    String header = headers.get(c);
                    if (header.isEmpty()) continue;
                    Cell cell = row.getCell(c);
                    String value = cell != null ? FORMATTER.formatCellValue(cell).trim() : "";
                    if (!value.isEmpty()) hasContent = true;
                    rowMap.put(header, value);
                }
                if (hasContent) {
                    rows.add(rowMap);
                }
            }
            return rows;
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file " + filePath + ": " + e.getMessage(), e);
        }
    }

    public static List<Map<String, String>> readSheet(String resourcePath, String sheetName, String... fallbacks) {
        Path resolved = ResourcePathResolver.resolve(resourcePath, fallbacks);
        return readSheet(resolved, sheetName);
    }
}
