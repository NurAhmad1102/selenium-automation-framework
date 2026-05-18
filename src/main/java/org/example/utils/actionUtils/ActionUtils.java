package org.example.utils.actionUtils;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class ActionUtils extends BasePage {

    public ActionUtils(WebDriver driver) {
        super(driver);
    }

    /**
     * buat cari element di dapan dropdown
     * cari by text
     *
     * @param textSearch
     */
    public void selectElementInDropdownByText(String textSearch){
        logger.info("Open Dropdown and Search for...{}", textSearch);

        if(textSearch == null || textSearch.trim().isEmpty()){
            logger.error("Dropdown: Keyword parameter empty");
        }

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // wait dropdown ke buka (gak hidden)
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//div[contains(@class,'ant-select-dropdown-hidden')]")
        ));

        Actions actions = new Actions(driver);
        int maxScrollAttempts = 30;
        int scrollCount = 0;
        boolean optionFound = false;

        while (!optionFound && scrollCount < maxScrollAttempts) {
            try {
                WebElement option = driver.findElement(By.xpath("//div[@class='ant-select-item-option-content' and text()='" + textSearch + "']"));

                // pake JS click kalo visibility gak guna
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", option);
                optionFound = true;
            } catch (NoSuchElementException e) {
                actions.sendKeys(Keys.DOWN).perform();
                scrollCount++;

                try {
                    Thread.sleep(300);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    logger.error("Interrupted while searching dropdown option: {}", textSearch, ex);
                    throw new RuntimeException("Interrupted while searching dropdown option: " + textSearch, ex);
                }
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
















































} // milik class


