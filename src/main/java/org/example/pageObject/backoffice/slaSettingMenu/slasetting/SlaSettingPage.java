package org.example.pageObject.backoffice.slaSettingMenu.slasetting;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SlaSettingPage extends BasePage {

    public SlaSettingPage(WebDriver driver){
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//p[contains(text(), 'Add SLA Setting')]")
    private WebElement addSlaSettingPage;

    @FindBy(id = "periodName")
    private WebElement periodNameField;

    @FindBy(id = "effectiveDate")
    private WebElement effectiveDateField;

    @FindBy(xpath = "//button[@class=\"ant-picker-year-btn\"]")
    private WebElement dateYear;

    @FindBy(xpath = "//button[@class = \"ant-picker-month-btn\"]")
    private WebElement dateMonth;

    private final String DATE_CELL = "//div[@class = \"ant-picker-cell-inner\"  and text()='%s']";

    @FindBy(xpath = "//button[@class = \"ant-picker-header-super-next-btn\"]")
    private WebElement nextYearButton;

    @FindBy(xpath = "//button[@class = \"ant-picker-header-super-prev-btn\"]")
    private WebElement prevYearButton;

    @FindBy(xpath = "//button[@class = \"ant-picker-header-next-btn\"]")
    private WebElement nextMonthButton;

    @FindBy(xpath = "//button[@type=\"button\" and contains(@class, \"ant-btn-primary\")]/span[text()=\"Save as Draft\"]")
    private WebElement saveAsDraftButton;

    @FindBy(xpath = "//button[span[text()='Submit']]")
    private WebElement submitButton;

    @FindBy(xpath = "//div[@class=\"ant-form-item-explain-error\" and text()=\"Please select a date\"]")
    private WebElement alertEffectiveDateEmpty;

    @FindBy(xpath = "//div[@class=\"ant-form-item-explain-error\" and text()=\"Please enter SLA period\"]")
    private WebElement alertSlaSettingPeriodEmpty;

//    SLA Setting Detail Section
    @FindBy(xpath = "//div[@class=\"ant-space-item\"]/div[text()=\"Add\"]")
    private WebElement addSlaDetailButton;

    @FindBy(xpath = "//div[@class=\"ant-space-item\"]/div[text()= \"Copy from Previous Setting\"]")
    private WebElement copyFromPreviousSettingButton;

    @FindBy(xpath = "//div[@class=\"ant-space-item\"]/div[text()= \"Delete\"]")
    private WebElement deleleSlaDetailButton;

    @FindBy(xpath = "//div[@class='ant-table-content']//table//tbody[@class='ant-table-tbody']/tr") //add class
    private List<WebElement> tableSlaSetting;

    public void isAddSlaSettingPage(){
        if (!isVisible(addSlaSettingPage)) {
            throw new RuntimeException("Add SLA Setting page header not visible");
        }
    }
 //TODO pindah ke modal addSla


    public WebElement setPeriodNameField(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOf(periodNameField));
    }

    public void clickEffectiveDateField(){
        click(effectiveDateField);
    }

    public String pickEffectiveDate(boolean futureEffDate) {
        logger.info("Open SLA effective date calendar");

        LocalDate currentDate = LocalDate.now();
        Random random = new Random();
        int dateValue;
        int monthValue;
        int expectedYear;

        if (futureEffDate) {
            dateValue = random.nextInt(1, 26);
            monthValue = random.nextInt(1, 13);
            int minYear = currentDate.getYear() + 1;
            int maxYear = minYear + 5;
            expectedYear = random.nextInt(minYear, maxYear + 1);
        } else {
            dateValue = currentDate.getDayOfMonth();
            monthValue = currentDate.getMonthValue();
            expectedYear = currentDate.getYear();
        }

        String expectedMonthName = Month.of(monthValue).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
        String expectedDate = dateValue < 10 ? "0" + dateValue : String.valueOf(dateValue);
        String expectedMonth = monthValue < 10 ? "0" + monthValue : String.valueOf(monthValue);

        logger.info("Selected date for SLA Effective date: {} {} {}", dateValue, monthValue, expectedYear);

        moveCalendarToMonth(expectedMonthName, getDateMonth().getText());
        moveCalendarToYear(expectedYear, Integer.parseInt(getDateYear().getText()));
        getDateCell(String.valueOf(dateValue)).click();

        return expectedDate + " " + expectedMonthName + " " + expectedYear;
    }

    private void moveCalendarToMonth(String expectedMonth, String actualMonth) {
        while (!expectedMonth.equals(actualMonth)) {
            clickNextMonthButton();
            actualMonth = getDateMonth().getText();
        }
    }

    private void moveCalendarToYear(int expectedYear, int actualYear) {
        while (expectedYear != actualYear) {
            if (expectedYear >= actualYear) {
                clickNextYearButton();
            } else {
                clickPrevYearButton();
            }
            actualYear = Integer.parseInt(getDateYear().getText());
        }
    }

    public WebElement getEffectiveDateField(){
        return effectiveDateField;
    }

    public WebElement getDateYear(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOf(dateYear));
    }

    public WebElement getDateMonth(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOf(dateMonth));
    }

    public WebElement getDateCell(String date){
        String xpath = String.format(DATE_CELL, date);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
    }

    public void clickNextYearButton(){
        click(nextYearButton);
    }

    public void clickPrevYearButton(){
        click(prevYearButton);
    }

    public void clickNextMonthButton(){
        click(nextMonthButton);
    }

    public void clickSaveAsDraftButton(){
        click(saveAsDraftButton);
    }

    public void clickSubmitButton(){
        click(submitButton);
    }

    public void isSlaSettingPeriodEmpty(){
        if (!isVisible(alertSlaSettingPeriodEmpty)) {
            throw new RuntimeException("SLA Period empty alert not visible");
        }
    }

    public void isEffectiveDateEmpty(){
        if (!isVisible(alertEffectiveDateEmpty)) {
            throw new RuntimeException("Effective Date empty alert not visible");
        }
    }




    //    SLA Setting Detail Section
    public void clickAddSlaDetailButton(){
        click(addSlaDetailButton);
    }

    public void clickCopyfromPreviousSettingButton(){
        click(copyFromPreviousSettingButton);
    }

    public void clickDeleteSlaDetailButton(){
        click(deleleSlaDetailButton);
    }

    public List<WebElement> getTableSlaSetting(){
        return tableSlaSetting;
    }

}
