package org.example.utils.actionUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ActionUtils {
    private final WebDriver driver;
    private final Logger logger = LogManager.getLogger(getClass());

    public ActionUtils(WebDriver driver) {
        this.driver = driver;
    }

    /**
     * buat cari element di dapan dropdown
     * cari by text
     *
     * @param textSearch
     */
    public void selectElementInDropdownByText(String textSearch) {
        logger.info("Open Dropdown and Search for...{}", textSearch);

        if (textSearch == null || textSearch.trim().isEmpty()) {
            logger.error("Dropdown: Keyword parameter empty");
            return;
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        By optionLocator = By.xpath("//div[@class='ant-select-item-option-content' and normalize-space()='" + textSearch + "']");

        // Wait for dropdown to be visible
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[contains(@class,'ant-select-dropdown-hidden')]")
        ));

        Actions actions = new Actions(driver);
        int maxScrollAttempts = 30;
        int scrollCount = 0;
        boolean optionFound = false;

        while (scrollCount < maxScrollAttempts) {
            try {
                WebElement option = driver.findElement(optionLocator);
                if (option.isDisplayed()) {
                    ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                    optionFound = true;
                    break;
                }
            } catch (NoSuchElementException e) {
                // Ignore and scroll
            }

            actions.sendKeys(Keys.DOWN).perform();
            scrollCount++;
            
            // Short wait for scroll to take effect
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (!optionFound) {
            throw new NoSuchElementException("Not found in dropdown list: " + textSearch);
        }
    }

    public void clearInputWithKeys(WebElement element) {
        logger.info("Clear Input Field for element {}", element);
        Actions actions = new Actions(driver);
        element.click();
        actions.keyDown(Keys.CONTROL).sendKeys("a").keyUp(Keys.CONTROL).sendKeys(Keys.DELETE).perform();
    }

    public void resetCheckboxes(By locator) {
        logger.info("Reset checkbox for locator {}", locator);
        List<WebElement> checkboxes = driver.findElements(locator);

        for (WebElement checkbox : checkboxes) {
            if (checkbox.isSelected()) {
                checkbox.click();
            }
        }
    }

    public String capitalizeFirstLetter(String action) {
        if (action == null || action.isEmpty()) {
            return action;
        }

        return action.substring(0, 1).toUpperCase() + action.substring(1).toLowerCase();
    }

    public void clickCoordinateWithJS(int x, int y) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("document.elementFromPoint(arguments[0], arguments[1]).click();", x, y);
    }













































} // milik class


