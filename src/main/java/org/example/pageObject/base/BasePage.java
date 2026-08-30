package org.example.pageObject.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.report.core.ReportAPI;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class BasePage {

    protected WebDriver driver;
    protected WebDriverWait wait;
    protected final int DEFAULT_TIMEOUT = 10;
    protected final Logger logger = LogManager.getLogger(getClass());

    public BasePage(WebDriver driver){

        if(driver == null){
            throw new IllegalArgumentException("WebDriver tidak boleh null!");
        }

        this.driver = driver;
        wait = new WebDriverWait(driver, Duration.ofSeconds(DEFAULT_TIMEOUT));
    }

    protected void click(WebElement element){
        String description = getElementDescription(element);
        try {
            logger.info("Clicking element: {}", description);

            wait.until(ExpectedConditions.elementToBeClickable(element));
            
            try {
                element.click();
            } catch (Exception e) {
                logger.warn("Standard click failed for [{}], attempting Javascript click. Reason: {}", description, e.getMessage());
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }
            
            ReportAPI.step("Action: Click element [" + description + "]");
        } catch (Exception e) {
            handleActionFailure("click", element, e);
        }
    }

    protected void click(By locator){
        try {
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            String description = getElementDescription(element);
            logger.info("Clicking locator [{}]: {}", locator, description);

            try {
                element.click();
            } catch (Exception e) {
                logger.warn("Standard click failed for locator [{}], attempting Javascript click with fresh element.", locator);
                WebElement freshElement = driver.findElement(locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", freshElement);
            }

            ReportAPI.step("Action: Click element [" + description + "]");
        } catch (Exception e) {
            handleActionFailure("click", locator, e);
        }
    }

//     This method is specifically for cases where standard click consistently fails due to complex UI layers, and we want to bypass Selenium's clickability checks.
    protected void clickWithJS(By locator) {
        try {
            logger.debug("Forcing JS Click on locator: {}", locator);
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
            String description = getElementDescription(element);
            logger.info("JS Click on element: {}", description);

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            ReportAPI.step("Action: JS Click element [" + description + "]");
        } catch (Exception e) {
            handleActionFailure("clickWithJS", locator, e);
        }
    }

    protected void typeText(WebElement element, String text){
        String description = getElementDescription(element);
        try {
            String displayText = (text != null && text.length() > 4)
                    ? text.substring(0, 2) + "****" + text.substring(text.length() - 2)
                    : "****";
            logger.info("Typing '{}' into element: {}", displayText, description);
            wait.until(ExpectedConditions.visibilityOf(element)).clear();
            element.sendKeys(text);
            ReportAPI.step("Action: Type text into element [" + description + "]");
        } catch (Exception e) {
            handleActionFailure("type text", element, e);
        }
    }

    protected String getAttributeValue(WebElement element, String attribute){
        try {
            logger.debug("Getting attribute '{}' from element: {}", attribute, getElementDescription(element));
            String value = element.getAttribute(attribute);
            ReportAPI.step("Action: Get attribute [" + attribute + "] from element [" + getElementDescription(element) + "]");
            return value;
        } catch (Exception e) {
            handleActionFailure("get attribute '" + attribute + "' from", element, e);
            return null;
        }
    }

    protected String getText(WebElement element) {
        try {
            logger.debug("Getting text from element: {}", getElementDescription(element));
            String text = wait.until(ExpectedConditions.visibilityOf(element)).getText();
            ReportAPI.step("Action: Get text from element [" + getElementDescription(element) + "]");
            return text;
        } catch (Exception e) {
            handleActionFailure("get text from", element, e);
            return null;
        }
    }

    protected String getText(By locator) {
        try {
            WebElement element = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            String description = getElementDescription(element);
            String text = element.getText();
            logger.debug("Getting text from locator [{}]: {}", locator, text);
            ReportAPI.step("Action: Get text from element [" + description + "]");
            return text;
        } catch (Exception e) {
            handleActionFailure("get text from", locator, e);
            return null;
        }
    }

//    IS VISIBLE
    protected boolean isVisible(WebElement element){
        String description = getElementDescription(element);
        try {
            boolean visible = wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed();
            logger.debug("Check visibility: Element [{}] is visible", description);
            ReportAPI.step("Check: Element [" + description + "] is visible");
            return visible;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Check visibility: Element [{}] is not visible", description);
            ReportAPI.step("Check: Element [" + description + "] is not visible");
            return false;
        }
    }

    protected boolean isVisible(By locator) {
        try {
            boolean visible = wait.until(ExpectedConditions.visibilityOfElementLocated(locator)).isDisplayed();
            String description = getElementDescription(driver.findElement(locator));
            logger.debug("Check visibility: Locator [{}] ({}) is visible", locator, description);
            ReportAPI.step("Check: Element [" + description + "] is visible");
            return visible;
        } catch (TimeoutException e) {
            logger.debug("Check visibility: Locator [{}] is not visible", locator);
            ReportAPI.step("Check: Locator [" + locator + "] is not visible");
            return false;
        }
    }

// INVISIBLE
    protected boolean isNotVisible(WebElement element){

        try {
            boolean invisible = wait.until(ExpectedConditions.invisibilityOf(element));
            logger.debug("Check invisibility: Element [{}] is hidden", element);
            ReportAPI.step("Check: Element [" + element + "] is hidden");
            return invisible;
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            logger.debug("Check invisibility: Element [{}] is still visible", element);
            ReportAPI.step("Check: Element [" + element + "] is still visible");
            return false;
        }
    }

    protected boolean isNotVisible(By locator) {

        try {
            boolean invisible = wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            logger.debug("Check invisibility: Locator [{}] is hidden", locator);
            ReportAPI.step("Check: Locator [" + locator + "] is hidden");
            return invisible;
        } catch (TimeoutException e) {
            logger.debug("Check invisibility: Locator [{}] is still visible", locator);
            ReportAPI.step("Check: Locator [" + locator + "] is still visible");
            return false;
        }
    }



    private void handleActionFailure(String action, Object target, Exception e) {
        String description = (target instanceof WebElement) ? getElementDescription((WebElement) target) : target.toString();
        String locatorInfo = (target instanceof WebElement) ? target.toString() : "Locator: " + target;
        
        String logConsoleMessage = String.format("Failed to %s element [%s]. Locator Detail: %s. Reason: %s", 
                action, description, locatorInfo, e.getMessage());

        String reportHtmlMessage = String.format("Failed to %s element [%s].<br><b>Locator Detail:</b> %s<br><b>Reason:</b> %s", 
                action, description, locatorInfo, e.getMessage());
        
        // Log clean plain text to Log4j2 console/file
        logger.error(logConsoleMessage);
        
        // Log to Structured Element Logs tab
        ReportAPI.elementError(action, locatorInfo, e);
        
        throw new RuntimeException(reportHtmlMessage, e);
    }

    public String getElementDescription(WebElement element) {
        try {
            String text = element.getText();
            if (isNotBlank(text)) return sanitizeDescription(text);

            text = element.getAttribute("aria-label");
            if (isNotBlank(text)) return sanitizeDescription(text);

            text = element.getAttribute("title");
            if (isNotBlank(text)) return sanitizeDescription(text);

            text = element.getAttribute("placeholder");
            if (isNotBlank(text)) return sanitizeDescription(text);

            text = element.getAttribute("data-testid");
            if (isNotBlank(text)) return sanitizeDescription(text);

            text = element.getAttribute("id");
            if (isNotBlank(text)) return sanitizeDescription(text);

            text = element.getAttribute("name");
            if (isNotBlank(text)) return sanitizeDescription(text);

            String tagName = element.getTagName();
            return isNotBlank(tagName) ? "[" + tagName + " element]" : "[unidentified element]";
        } catch (Exception e) {
            return "[unable to inspect element]";
        }
    }

    private String sanitizeDescription(String input) {
        if (input == null) return "";
        String trimmed = input.trim().replaceAll("\\s+", " ");
        return trimmed.length() > 50 ? trimmed.substring(0, 47) + "..." : trimmed;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }

//    DROPDOWN
    protected void selectDropdownModal(WebElement element, String option){
        String description = getElementDescription(element);
        logger.info("Selecting option [{}] from dropdown [{}]", option, description);
        ReportAPI.step("Action: Select option [" + option + "] from dropdown [" + description + "]");
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
        By searchInputLocator = By.xpath("//div[@role='listbox']//input[@type='text']");
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputLocator));
        typeText(searchInput, value);

        selectOption(value);
    }

    private void selectOption(String value) {
        By optionLocator = By.xpath(String.format("//div[@role='option'][contains(normalize-space(.), '%s')]", value));
        
        wait.until(ExpectedConditions.presenceOfElementLocated(optionLocator));
        List<WebElement> options = driver.findElements(optionLocator);
        WebElement target = null;

        for (WebElement opt : options) {
            try {
                if (opt.isDisplayed()) {
                    target = opt;
                    break;
                }
            } catch (StaleElementReferenceException ignored) {
                // If stale, we'll try to re-locate below
            }
        }

        if (target == null) {
            try {
                WebElement listbox = driver.findElement(By.xpath("//div[@role='listbox']"));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollTop = arguments[0].scrollHeight;",
                        listbox
                );
            } catch (Exception ignored) {}

            target = wait.until(ExpectedConditions.elementToBeClickable(optionLocator));
        }

        click(target);
    }


    private boolean isSearchableDropdown(){
        By searchInputLocator = By.xpath("//div[@role='listbox']//input[@type='text']");
        try {
            // Give it a small window to appear if it's dynamic
            new WebDriverWait(driver, Duration.ofMillis(500))
                    .until(ExpectedConditions.presenceOfElementLocated(searchInputLocator));
            return true;
        } catch (TimeoutException e) {
            return false;
        }
    }

    protected boolean isElementClickable(WebElement element) {
        String description = getElementDescription(element);
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            logger.debug("Check clickability: Element [{}] is clickable", description);
            ReportAPI.step("Check: Element [" + description + "] is clickable");
            return true;
        } catch (TimeoutException e) {
            logger.debug("Check clickability: Element [{}] is not clickable", description);
            ReportAPI.step("Check: Element [" + description + "] is not clickable");
            return false;
        }
    }

    protected boolean isElementClickable(By element) {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(element));
            String description = getElementDescription(driver.findElement(element));
            logger.debug("Check clickability: Locator [{}] ({}) is clickable", element, description);
            ReportAPI.step("Check: Element [" + description + "] is clickable");
            return true;
        } catch (TimeoutException e) {
            logger.debug("Check clickability: Locator [{}] is not clickable", element);
            ReportAPI.step("Check: Locator [" + element + "] is not clickable");
            return false;
        }
    }

    protected boolean isClickable(WebElement element) {
        String description = getElementDescription(element);
        try {
            boolean clickable = element.isDisplayed() && element.isEnabled();
            logger.debug("Check clickability: Element [{}] isClickable={}", description, clickable);
            ReportAPI.step("Check: Element [" + description + "] isClickable=" + clickable);
            return clickable;
        } catch (Exception e) {
            logger.debug("Check clickability: Element [{}] is not clickable/present", description);
            return false;
        }
    }

    protected boolean isNotClickable(WebElement element) {
        return !isClickable(element);
    }

    protected boolean isPresent(By locator) {
        try {
            boolean present = !driver.findElements(locator).isEmpty();
            logger.debug("Check presence: Locator [{}] isPresent={}", locator, present);
            return present;
        } catch (Exception e) {
            return false;
        }
    }

    protected void scrollMoveToElement(WebElement element) {
        Actions actions = new Actions(driver);
        try {
            actions.moveToElement(element).perform();
            String description = getElementDescription(element);
            logger.debug("Check element after scrolling: Element [{}] isFound={}", description, element);
            ReportAPI.step("Check: Element [" + description + "] isFound=" + element);
        } catch (Exception e) {
            logger.debug("Check element after scrolling: Element [{}] is not clickable/present", element);
        }

    }

    protected void scrollMoveToElement(By element) {
        Actions actions = new Actions(driver);

        try {
            WebElement ele = driver.findElement(element);
            actions.moveToElement(ele).perform();
            String description = getElementDescription(ele);
            logger.debug("Check element after scrolling: Element [{}] isFound={}", description, element);
            ReportAPI.step("Check: Element [" + description + "] isFound=" + element);
        } catch (Exception e) {
            logger.debug("Check element after scrolling: Element [{}] is not found", element);
        }
    }

}
