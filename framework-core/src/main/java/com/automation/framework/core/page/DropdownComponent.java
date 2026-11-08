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

    public DropdownComponent(WebDriver driver) {
        this(driver, new WebDriverWait(driver, Duration.ofSeconds(15)));
    }

    public DropdownComponent(WebDriver driver, WebDriverWait wait) {
        this.driver = driver;
        this.wait = wait;
    }

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

    public void selectSearchableDropdown(String value) {
        By searchInputLocator = By.xpath("//div[@role='listbox']//input[@type='text']");
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputLocator));

        searchInput.clear();
        searchInput.sendKeys(value);

        try {
            selectOption(value);
        } catch (Exception e) {
            logger.warn("Option [{}] not found immediately after filtering. Clearing search box to inspect all available options...", value);
            try {
                searchInput.sendKeys(Keys.chord(Keys.CONTROL, "a"), Keys.BACK_SPACE);
                selectOption(value);
            } catch (Exception ex) {
                logger.error("Failed to select option [{}] from dropdown. Error: {}", value, ex.getMessage());
                throw ex;
            }
        }
    }

    public void selectScrollableDropdown(String value) {
        selectOption(value);
    }

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
