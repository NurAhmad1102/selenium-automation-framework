package org.example.pageObject.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

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

}
