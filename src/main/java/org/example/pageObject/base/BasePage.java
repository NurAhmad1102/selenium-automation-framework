package org.example.pageObject.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final Logger logger = LogManager.getLogger(getClass());

    public BasePage(WebDriver driver){

        if(driver == null){
            throw new IllegalArgumentException("WebDriver tidak boleh null!");
        }

        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    protected void click(WebElement element){
        logger.info("Click for: {}", getElementDescription(element));
        wait.until(ExpectedConditions.elementToBeClickable(element)).click();
    }

    protected void click(By locator){
        logger.info("Click for locator: {}", locator);
        wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
    }

    protected void typeText(WebElement element, String text){
        String displayText = (text != null && text.length() > 4)
                ? text.substring(0, 2) + "****" + text.substring(text.length() - 2)
                : "****";
        logger.debug("Typing into element: {}", displayText);
        wait.until(ExpectedConditions.visibilityOf(element)).clear();
        element.sendKeys(text);
    }

    protected String getAttributeValue(WebElement element, String attribute){
        return element.getAttribute(attribute);
    }

//    protected String getText(WebElement element){
//        return wait.until(ExpectedConditions.visibilityOf(element)).getText();
//    }

    protected boolean isVisible(WebElement element){
        try {
            return wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    protected boolean isVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator))
                    .isDisplayed();
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected void waitForElementToDisappear(WebElement element){
        wait.until(ExpectedConditions.invisibilityOf(element));
    }

    private String getElementDescription(WebElement element) {
        String text = element.getText();
        if (text == null || text.trim().isEmpty()) {
            text = element.getAttribute("aria-label");
        }
        if (text == null || text.trim().isEmpty()) {
            text = element.getAttribute("title");
        }
        if (text == null || text.trim().isEmpty()) {
            text = element.getAttribute("class");
        }
        if (text == null || text.trim().isEmpty()) {
            text = element.getAttribute("id");
        }

        return text != null ? text : "[no text]";
    }

    // bekerja, jaga-jaga untuk rollback jika refactor failed
//    protected void selectDropdownModal(WebElement eleDropdown, String value) {
//        logger.info("Step: Open Dropdown and select: {}", value);
//        click(eleDropdown);
//
//        WebElement searchInput = wait.until(
//                ExpectedConditions.visibilityOfElementLocated(
//                        By.xpath("//div[@role='listbox']//input[@type='text']")
//                )
//        );
//
//        logger.info("Step: Clear Field Input");
//        searchInput.clear();
//        logger.info("Step: Type value to search input");
//        typeText(searchInput, value);
//
//
//        String xpath = String.format("""
//        //*[
//            (
//                @role='option'
//                or @role='menuitem'
//                or self::li
//                or self::button
//                or contains(@class,'option')
//                or contains(@class,'menu-item')
//            )
//            and normalize-space(.)='%s'
//        ]
//        """, value);
//
//        WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
//        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
//
//        click(driver.findElement(By.xpath(xpath)));
//    }

    protected void selectDropdownModal(WebElement element, String option){
        logger.info("Step: Open Dropdown and select: {}", option);
        click(element);

        if(isSearchableDropdown()){
            selectSearchableDropdown(option);
        } else {
            selectScrollableDropdown(option);
        }
    }

    private void selectScrollableDropdown(String value){
        selectOption(value);
    }

    private void selectSearchableDropdown(String value) {
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(
                        By.xpath("//div[@role='listbox']//input[@type='text']")
                )
        );

        searchInput.clear();
        typeText(searchInput, value);

        selectOption(value);
    }

//    Locator general/buat semua
    private void selectOption(String value) {

        By optionLocator = By.xpath(String.format("""
                //div[@role='option']
                [contains(normalize-space(.), '%s')]
                """, value));

        wait.until(ExpectedConditions.presenceOfElementLocated(optionLocator));

        List<WebElement> options = driver.findElements(optionLocator);

        WebElement target = null;

        for (WebElement opt : options) {
            if (opt.isDisplayed()) {
                target = opt;
                break;
            }
        }

        if (target == null) {

            WebElement listbox = driver.findElement(
                    By.xpath("//div[@role='listbox']")
            );

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollTop = arguments[0].scrollHeight;",
                    listbox
            );

            target = wait.until(
                    ExpectedConditions.elementToBeClickable(optionLocator)
            );
        }

        click(target);
    }


    private boolean isSearchableDropdown(){
        return !driver.findElements(
                By.xpath("//div[@role='listbox']//input[@type='text']")
        ).isEmpty();
    }

}
