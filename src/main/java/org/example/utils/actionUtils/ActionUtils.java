package org.example.utils.actionUtils;

import org.example.pageObject.base.BasePage;
import org.example.pageObject.backoffice.slaSettingMenu.SlaSetting.SlaSettingPage;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class ActionUtils extends BasePage {

    SlaSettingPage slaSettingPage;

    public ActionUtils(WebDriver driver) {
        super(driver);
        slaSettingPage = new SlaSettingPage(driver);
    }

    /**
     * buat handle pick Calendar
     * kalo @param True bakal click future date, False click date now
     * furure date akan pick 1 tahun dari sekarang
     *
     * @param isFutureEffDate tentuin date yg akan di set ke field effective date
     * @return
     */
    public String setPickDateCalender(boolean isFutureEffDate){
        logger.info("Open Calender...");

        final String expectedDate;
        final String expectedMonth;

        LocalDate date = LocalDate.now();
        Random random = new Random();
        int dateValue;
        int monthValue;
        int expectedYear;

        if(isFutureEffDate){
            dateValue = random.nextInt(1, 26);
            monthValue = random.nextInt(1,13);

            int minYear = date.getYear() + 1;
            int maxYear = minYear + 5;
            expectedYear = random.nextInt(minYear, maxYear+1);
        } else {
            dateValue = date.getDayOfMonth();
            monthValue = date.getMonthValue();
            expectedYear = date.getYear();
        }

        Month month = Month.of(monthValue);
        String monthName = month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);

        if (monthValue < 10){
            expectedMonth = "0"+ monthValue;
        } else {
            expectedMonth = String.valueOf(monthValue);
        }

        String selectedEffDate = dateValue+" "+ monthValue + " "+ expectedYear;
        logger.info("Selected date for SLA Effective date: {}", selectedEffDate);

        int actualYear = Integer.parseInt(slaSettingPage.getDateYear().getText());
        String actualMonth = slaSettingPage.getDateMonth().getText();

        isMonth(monthName, actualMonth);
        isYear(expectedYear, actualYear);
        slaSettingPage.getDateCell(String.valueOf(dateValue)).click();

        if(dateValue < 10){
            expectedDate = "0" + dateValue;
        } else {
            expectedDate = String.valueOf(dateValue);
        }

        return expectedDate +"/"+
               expectedMonth +"/"+
               expectedYear;
    }


    private void isMonth(String inputMonth, String actualMonth){
        while (!inputMonth.equals(actualMonth)){
            slaSettingPage.clickNextMonthButton();
            actualMonth = slaSettingPage.getDateMonth().getText();
        }
    }

    private void isYear(int inputYear, int actualYear){
        while (inputYear != actualYear){
            if(inputYear >= actualYear){
                slaSettingPage.clickNextYearButton();
            } else {
                slaSettingPage.clickPrevYearButton();
            }
            actualYear = Integer.parseInt(slaSettingPage.getDateYear().getText());
        }
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


