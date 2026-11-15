package com.automation.framework.core.page;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Agnostic component to handle modern listbox dropdowns (e.g., Radix UI, Ant Design, Material)
 * supporting both searchable filters and scrollable option containers.
 */
public class DropdownComponent {
    private final Logger logger = LogManager.getLogger(getClass());
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By triggerLocator;
    private final WebElement triggerElement;

    /**
     * Constructs a DropdownComponent with default 15-second wait.
     *
     * @param driver active WebDriver instance
     */
    public DropdownComponent(WebDriver driver) {
        this(driver, new WebDriverWait(driver, Duration.ofSeconds(15)), null, null);
    }

    /**
     * Constructs a DropdownComponent with custom WebDriverWait.
     *
     * @param driver active WebDriver instance
     * @param wait custom WebDriverWait instance
     */
    public DropdownComponent(WebDriver driver, WebDriverWait wait) {
        this(driver, wait, null, null);
    }

    /**
     * Constructs a DropdownComponent with a trigger locator.
     *
     * @param driver active WebDriver instance
     * @param triggerLocator locator for the element that opens the dropdown menu
     */
    public DropdownComponent(WebDriver driver, By triggerLocator) {
        this(driver, new WebDriverWait(driver, Duration.ofSeconds(15)), triggerLocator, null);
    }

    /**
     * Constructs a DropdownComponent with a trigger WebElement.
     *
     * @param driver active WebDriver instance
     * @param triggerElement WebElement that opens the dropdown menu
     */
    public DropdownComponent(WebDriver driver, WebElement triggerElement) {
        this(driver, new WebDriverWait(driver, Duration.ofSeconds(15)), null, triggerElement);
    }

    private DropdownComponent(WebDriver driver, WebDriverWait wait, By triggerLocator, WebElement triggerElement) {
        this.driver = driver;
        this.wait = wait;
        this.triggerLocator = triggerLocator;
        this.triggerElement = triggerElement;
    }

    /**
     * Opens the dropdown menu by clicking the trigger element if configured.
     */
    public void open() {
        if (triggerLocator != null) {
            logger.info("Opening dropdown via trigger locator: {}", triggerLocator);
            wait.until(ExpectedConditions.elementToBeClickable(triggerLocator)).click();
        } else if (triggerElement != null) {
            logger.info("Opening dropdown via trigger element: {}", triggerElement);
            wait.until(ExpectedConditions.elementToBeClickable(triggerElement)).click();
        }
    }

    /**
     * Opens the dropdown (if trigger is configured) and selects the option matching the provided text.
     *
     * @param text option label to select
     */
    public void selectByText(String text) {
        open();
        selectOption(text);
    }

    /**
     * Opens the dropdown (if trigger is configured), filters by typing searchQuery, and selects the matching option.
     *
     * @param searchQuery search query to filter options
     * @param optionText option text to select
     */
    public void searchAndSelect(String searchQuery, String optionText) {
        open();
        selectSearchableDropdown(searchQuery, optionText);
    }

    /**
     * Checks if the currently open listbox contains a text search/filter input.
     *
     * @return true if search input is present
     */
    public boolean isSearchableDropdown() {
        By searchInputLocator = By.xpath("//div[@role='listbox']//input[@type='text']");
        try {
            new WebDriverWait(driver, Duration.ofMillis(500))
                    .until(ExpectedConditions.presenceOfElementLocated(searchInputLocator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    /**
     * Filters the searchable dropdown by typing the value into the input field and selecting the matching option.
     *
     * @param value option text to filter and select
     */
    public void selectSearchableDropdown(String value) {
        selectSearchableDropdown(value, value);
    }

    /**
     * Filters the searchable dropdown by typing searchQuery into the filter input, then selecting optionText.
     *
     * @param searchQuery search query to filter options
     * @param optionText option text to find and click
     */
    public void selectSearchableDropdown(String searchQuery, String optionText) {
        By searchInputLocator = By.xpath("//div[@role='listbox']//input[@type='text']");
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputLocator));

        searchInput.clear();
        searchInput.sendKeys(searchQuery);

        try {
            selectOption(optionText);
        } catch (Exception e) {
            logger.warn("Option [{}] not found immediately after filtering with '{}'. Clearing search box to inspect all available options...", optionText, searchQuery);
            try {
                searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
                selectOption(optionText);
            } catch (Exception ex) {
                logger.error("Failed to select option [{}] from dropdown. Error: {}", optionText, ex.getMessage());
                throw ex;
            }
        }
    }

    /**
     * Selects an option from a scrollable dropdown container.
     *
     * @param value option label to select
     */
    public void selectScrollableDropdown(String value) {
        selectOption(value);
    }

    /**
     * Generic option selector from an open listbox container (div[role='listbox']).
     * Handles visibility checks, fuzzy text matching, virtual scrolling fallback, and JavaScript click.
     *
     * @param value option text to find and click
     */
    public void selectOption(String value) {
        By optionLocator = By.xpath(String.format("//div[@role='option'][contains(normalize-space(.), '%s')]", value));

        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div[@role='option']")));
        } catch (Exception ignored) {
        }

        List<WebElement> options = driver.findElements(optionLocator);
        WebElement target = null;

        for (WebElement opt : options) {
            try {
                if (opt.isDisplayed()) {
                    target = opt;
                    break;
                }
            } catch (StaleElementReferenceException ignored) {
            }
        }

        if (target == null) {
            List<WebElement> allOptions = driver.findElements(By.xpath("//div[@role='option']"));
            logger.info("Total options found in listbox: {}", allOptions.size());
            for (WebElement opt : allOptions) {
                try {
                    String optText = opt.getText().trim();
                    if (optText.toLowerCase().contains(value.toLowerCase()) || value.toLowerCase().contains(optText.toLowerCase())) {
                        logger.info("Found matching option via fallback contains: '{}'", optText);
                        target = opt;
                        break;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (target == null) {
            try {
                WebElement listbox = driver.findElement(By.xpath("//div[@role='listbox']"));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollTop = arguments[0].scrollHeight;",
                        listbox
                );
            } catch (Exception ignored) {
            }

            target = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
        }

        try {
            target.click();
        } catch (ElementClickInterceptedException e) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", target);
        }
    }
}
