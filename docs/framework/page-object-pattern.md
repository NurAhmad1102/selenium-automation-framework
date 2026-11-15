# Page Object Pattern & Component Guide

The framework enforces the **Page Object Model (POM)** and **Component Object Model (COM)** design patterns with built-in resilience, explicit wait strategies, and strict architectural boundaries.

---

## 1. Principles of Page Objects

1. **Encapsulation**: HTML element locators (`By`) and raw interactions (`click()`, `sendKeys()`) belong strictly inside Page Objects.
2. **Explicit Waits by Default**: Never use `Thread.sleep()`. All actions in `BasePage` automatically wait for element visibility, clickability, or presence.
3. **Lazy Page Factories**: Rather than eagerly instantiating multiple Page Objects in a test, use Factory classes with supplier/getter patterns to initialize pages only when they are called.
4. **ArchUnit Boundary Enforcement**: Test classes should not instantiate page objects directly with `new MyPage(driver)`; access them through standard page factories or test base helpers.

---

## 2. Implementing a Page Object

Inherit from `com.automation.framework.core.page.BasePage`:

```java
package com.mycompany.app.pages;

import com.automation.framework.core.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class InventoryPage extends BasePage {

    // Locators
    private final By headerTitle = By.cssSelector(".title");
    private final By searchInput = By.id("search-query");
    private final By applyFilterBtn = By.cssSelector("button[type='submit']");
    private final By itemList = By.cssSelector(".inventory-item");

    public InventoryPage(WebDriver driver) {
        super(driver);
    }

    public String getHeaderTitle() {
        return getText(headerTitle);
    }

    public InventoryPage searchItem(String name) {
        typeText(searchInput, name);
        click(applyFilterBtn);
        return this;
    }

    public int getItemCount() {
        return driver.findElements(itemList).size();
    }
}
```

---

## 3. BasePage Core Capabilities

`BasePage` provides robust, auto-recovering methods:

| Method | Behavior |
|--------|----------|
| `click(By locator)` | Waits until clickable, attempts standard click, automatically falls back to JavaScript `arguments[0].click()` if intercepted |
| `typeText(By locator, String text)` | Waits for visibility, clears input, types text (alias: `type`) |
| `getText(By locator)` | Waits for visibility, retrieves visible text |
| `isVisible(By locator)` | Safely returns `false` on `TimeoutException`, `NoSuchElementException`, or DOM detach (alias: `isDisplayed`) |
| `isNotVisible(By locator)` | Safely verifies element absence or hidden state without throwing exceptions |
| `scrollMoveToElement(By locator)` | Smoothly scrolls element into viewport via JavaScript |
| `isElementClickable(By locator)` | Checks if element is present, visible, and enabled for interaction |

---

## 4. Reusable UI Components: DropdownComponent

For modern UI libraries (Radix UI, Headless UI, Material UI, Shadcn) that render dropdowns as custom virtual lists rather than standard `<select>` tags:

```java
import com.automation.framework.core.page.DropdownComponent;

public class SettingsPage extends BasePage {
    private final By countryDropdownTrigger = By.id("country-select-trigger");

    public SettingsPage selectCountry(String countryName) {
        DropdownComponent dropdown = new DropdownComponent(driver, countryDropdownTrigger);
        dropdown.selectByText(countryName);
        return this;
    }

    public SettingsPage searchAndSelectCountry(String searchQuery, String optionText) {
        DropdownComponent dropdown = new DropdownComponent(driver, countryDropdownTrigger);
        dropdown.searchAndSelect(searchQuery, optionText);
        return this;
    }
}
```

---

## 5. Working with HTML Tables: WebTable

Inspect and parse structured tabular data cleanly:

```java
import com.automation.framework.core.table.WebTable;

public class UsersPage extends BasePage {
    private final By usersTableLocator = By.id("users-table");

    public int getUserRowIndex(String userEmail) {
        WebTable table = new WebTable(driver, usersTableLocator);
        return table.findRowIndexByCellValue("Email", userEmail);
    }

    public void clickEditUser(String userEmail) {
        WebTable table = new WebTable(driver, usersTableLocator);
        int rowIndex = table.findRowIndexByCellValue("Email", userEmail);
        table.getCellElement(rowIndex, "Action").findElement(By.cssSelector(".edit-btn")).click();
    }
}
```
