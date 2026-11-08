package com.automation.framework.core.table;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agnostic component to inspect and interact with standard HTML data tables.
 */
public class WebTable {

    private final WebElement tableElement;

    public WebTable(WebElement tableElement) {
        if (tableElement == null) {
            throw new IllegalArgumentException("Table element must not be null!");
        }
        this.tableElement = tableElement;
    }

    public List<WebElement> getRows() {
        return tableElement.findElements(By.xpath(".//tbody/tr"));
    }

    public int getRowCount() {
        return getRows().size();
    }

    public List<WebElement> getHeaders() {
        return tableElement.findElements(By.xpath(".//thead//th | .//tr[1]//th"));
    }

    public List<String> getHeaderTexts() {
        return getHeaders().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    public List<WebElement> getCellsInRow(int rowIndex) {
        List<WebElement> rows = getRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + rowIndex + " (Total rows: " + rows.size() + ")");
        }
        return rows.get(rowIndex).findElements(By.tagName("td"));
    }

    public String getCellValue(int rowIndex, int colIndex) {
        List<WebElement> cells = getCellsInRow(rowIndex);
        if (colIndex < 0 || colIndex >= cells.size()) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " + colIndex + " (Total columns: " + cells.size() + ")");
        }
        return cells.get(colIndex).getText().trim();
    }

    public int findRowIndexByText(String text, int targetColumnIndex) {
        List<WebElement> rows = getRows();
        for (int i = 0; i < rows.size(); i++) {
            List<WebElement> cells = rows.get(i).findElements(By.tagName("td"));
            if (targetColumnIndex >= 0 && targetColumnIndex < cells.size()) {
                if (cells.get(targetColumnIndex).getText().trim().equalsIgnoreCase(text.trim())) {
                    return i;
                }
            }
        }
        return -1;
    }

    public static boolean searchCellByText(WebDriver driver, String keyword) {
        String xpath = String.format("//td[contains(normalize-space(text()), '%s')]", keyword);
        return !driver.findElements(By.xpath(xpath)).isEmpty();
    }
}
