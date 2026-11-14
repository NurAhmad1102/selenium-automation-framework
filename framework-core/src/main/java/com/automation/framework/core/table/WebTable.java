package com.automation.framework.core.table;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Agnostic component to inspect and interact with standard HTML data tables.
 * Provides helper methods to retrieve rows, headers, cell values, and search
 * for specific text across table cells.
 */
public class WebTable {

    private final WebElement tableElement;

    /**
     * Constructs a WebTable instance wrapping the provided table WebElement.
     *
     * @param tableElement the HTML table element, must not be null
     * @throws IllegalArgumentException if tableElement is null
     */
    public WebTable(WebElement tableElement) {
        if (tableElement == null) {
            throw new IllegalArgumentException("Table element must not be null!");
        }
        this.tableElement = tableElement;
    }

    /**
     * Returns all row WebElements located inside tbody.
     *
     * @return List of row WebElements
     */
    public List<WebElement> getRows() {
        return tableElement.findElements(By.xpath(".//tbody/tr"));
    }

    /**
     * Returns the total count of rows currently visible inside tbody.
     *
     * @return row count
     */
    public int getRowCount() {
        return getRows().size();
    }

    /**
     * Returns header elements located in thead or the first table row.
     *
     * @return List of header WebElements
     */
    public List<WebElement> getHeaders() {
        return tableElement.findElements(By.xpath(".//thead//th | .//tr[1]//th"));
    }

    /**
     * Extracts and trims text from all table header cells.
     *
     * @return List of trimmed header label strings
     */
    public List<String> getHeaderTexts() {
        return getHeaders().stream()
                .map(WebElement::getText)
                .map(String::trim)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all cell WebElements for a specific zero-based row index.
     *
     * @param rowIndex zero-based row index
     * @return List of cell WebElements
     * @throws IndexOutOfBoundsException if rowIndex is invalid
     */
    public List<WebElement> getCellsInRow(int rowIndex) {
        List<WebElement> rows = getRows();
        if (rowIndex < 0 || rowIndex >= rows.size()) {
            throw new IndexOutOfBoundsException("Row index out of bounds: " + rowIndex + " (Total rows: " + rows.size() + ")");
        }
        return rows.get(rowIndex).findElements(By.tagName("td"));
    }

    /**
     * Retrieves the text value of a specific cell by row and column index.
     *
     * @param rowIndex zero-based row index
     * @param colIndex zero-based column index
     * @return trimmed text value of the cell
     * @throws IndexOutOfBoundsException if row or column index is out of bounds
     */
    public String getCellValue(int rowIndex, int colIndex) {
        List<WebElement> cells = getCellsInRow(rowIndex);
        if (colIndex < 0 || colIndex >= cells.size()) {
            throw new IndexOutOfBoundsException("Column index out of bounds: " + colIndex + " (Total columns: " + cells.size() + ")");
        }
        return cells.get(colIndex).getText().trim();
    }

    /**
     * Finds the first row index where the cell at targetColumnIndex matches the given text.
     *
     * @param text expected cell text (case-insensitive)
     * @param targetColumnIndex zero-based column index to check
     * @return zero-based row index, or -1 if not found
     */
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

    /**
     * Checks if any cell across the page contains the specified keyword.
     *
     * @param driver active WebDriver instance
     * @param keyword text keyword to search for
     * @return true if at least one matching cell is found, false otherwise
     */
    public static boolean searchCellByText(WebDriver driver, String keyword) {
        String xpath = String.format("//td[contains(normalize-space(text()), '%s')]", keyword);
        return !driver.findElements(By.xpath(xpath)).isEmpty();
    }
}
