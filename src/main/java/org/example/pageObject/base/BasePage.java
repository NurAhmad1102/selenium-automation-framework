package org.example.pageObject.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.report.core.ReportAPI;
import org.openqa.selenium.*;
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
            logger.debug("Clicking element: {}", description);
            // Ensure element is stable and clickable
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
            logger.debug("Clicking locator: {}", locator);
            WebElement element = wait.until(ExpectedConditions.elementToBeClickable(locator));
            
            // Stabilization wait to handle async UI state changes (False Clicks)
            try { Thread.sleep(500); }
            catch (InterruptedException ignored) {}

            try {
                element.click();
            } catch (Exception e) {
                logger.warn("Standard click failed for locator [{}], attempting Javascript click.", locator);
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            }

            ReportAPI.step("Action: Click locator [" + locator + "]");
        } catch (Exception e) {
            handleActionFailure("click", locator, e);
        }
    }

//     This method is specifically for cases where standard click consistently fails due to complex UI layers, and we want to bypass Selenium's clickability checks.
    protected void clickWithJS(By locator) {
        try {
            logger.debug("Forcing JS Click on locator: {}", locator);
            // tunggu sampai elemen ada di sialan muncul DOM, tidak buthu nunggu clickable
            WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(locator));

            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
            ReportAPI.step("Action: JS Click locator [" + locator + "]");
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
            logger.debug("Typing '{}' into element: {}", displayText, description);
            wait.until(ExpectedConditions.visibilityOf(element)).clear();
            element.sendKeys(text);
            ReportAPI.step("Action: Type text into element [" + description + "]");
        } catch (Exception e) {
            handleActionFailure("type text", element, e);
        }
    }

    protected String getAttributeValue(WebElement element, String attribute){
        try {
            String value = element.getAttribute(attribute);
            ReportAPI.step("Action: Get attribute [" + attribute + "] from element [" + getElementDescription(element) + "]");
            return value;
        } catch (Exception e) {
            handleActionFailure("get attribute '" + attribute + "' from", element, e);
            return null;
        }
    }

//    IS VISIBLE
    protected boolean isVisible(WebElement element){
        try {
            return wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            String msg = "Element not visible: " + getElementDescription(element);
            logger.debug(msg);
            ReportAPI.elementError("visibility check", element.toString(), e);
            return false;
        }
    }

    protected boolean isVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.visibilityOfElementLocated(locator))
                    .isDisplayed();
        } catch (TimeoutException e) {
            String msg = "Locator not visible: " + locator;
            logger.debug(msg);
            ReportAPI.elementError("visibility check", "Locator: " + locator, e);
            return false;
        }
    }

// INVISIBLE
    protected boolean isNotVisible(WebElement element){
        try {
            return wait.until(ExpectedConditions.invisibilityOf(element));
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            String msg = "Element still visible: " + getElementDescription(element);
            logger.debug(msg);
            ReportAPI.elementError("visibility check", element.toString(), e);
            return false;
        }
    }

    protected boolean isNotVisible(By locator) {
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        } catch (TimeoutException e) {
            String msg = "Locator still visible: " + locator;
            logger.debug(msg);
            ReportAPI.elementError("visibility check", "Locator: " + locator, e);
            return false;
        }
    }



    private void handleActionFailure(String action, Object target, Exception e) {
        String description = (target instanceof WebElement) ? getElementDescription((WebElement) target) : target.toString();
        String locatorInfo = (target instanceof WebElement) ? target.toString() : "Locator: " + target;
        
        String errorMessage = String.format("Failed to %s element [%s].<br><b>Locator Detail:</b> %s<br><b>Reason:</b> %s", 
                action, description, locatorInfo, e.getMessage());
        
        // Log to Log4j2
        logger.error(errorMessage);
        
        // Log to Structured Element Logs tab
        ReportAPI.elementError(action, locatorInfo, e);
        
        throw new RuntimeException(errorMessage, e);
    }

    public String getElementDescription(WebElement element) {
        try {
            String text = element.getText();
            if (text == null || text.trim().isEmpty()) {
                text = element.getAttribute("aria-label");
            }
            if (text == null || text.trim().isEmpty()) {
                text = element.getAttribute("title");
            }
            if (text == null || text.trim().isEmpty()) {
                text = element.getAttribute("placeholder");
            }
            if (text == null || text.trim().isEmpty()) {
                text = element.getAttribute("class");
            }
            if (text == null || text.trim().isEmpty()) {
                text = element.getAttribute("id");
            }

            return text != null && !text.isEmpty() ? text : "[no identifiable text]";
        } catch (Exception e) {
            return "[unable to get element description]";
        }
    }

//    DROPDOWN
    protected void selectDropdownModal(WebElement element, String option){
        logger.info("Step: Open Dropdown and select: {}", option);
        click(element);

        // Wait a bit for dropdown to potentially stabilize
        try { Thread.sleep(200); } catch (InterruptedException ignored) {}

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
        
        // Wait for search input to be visible and fresh
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(searchInputLocator));

        // Use typeText directly with the locator or fresh element to benefit from its internal wait/retry
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
//

}
