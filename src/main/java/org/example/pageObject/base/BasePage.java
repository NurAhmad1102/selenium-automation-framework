package org.example.pageObject.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.utils.DiagnosticCollector;
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
        try {
            logger.debug("Clicking element: {}", getElementDescription(element));
            wait.until(ExpectedConditions.elementToBeClickable(element)).click();
        } catch (Exception e) {
            handleActionFailure("click", element, e);
        }
    }

    protected void click(By locator){
        try {
            logger.debug("Clicking locator: {}", locator);
            wait.until(ExpectedConditions.elementToBeClickable(locator)).click();
        } catch (Exception e) {
            handleActionFailure("click", locator, e);
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
        } catch (Exception e) {
            handleActionFailure("type text", element, e);
        }
    }

    protected String getAttributeValue(WebElement element, String attribute){
        try {
            return element.getAttribute(attribute);
        } catch (Exception e) {
            handleActionFailure("get attribute '" + attribute + "' from", element, e);
            return null;
        }
    }

    protected boolean isVisible(WebElement element){
        try {
            return wait.until(ExpectedConditions.visibilityOf(element)).isDisplayed();
        } catch (TimeoutException | NoSuchElementException | StaleElementReferenceException e) {
            String msg = "Element not visible: " + getElementDescription(element);
            logger.debug(msg);
            DiagnosticCollector.addDiagnostic(msg);
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
            DiagnosticCollector.addDiagnostic(msg);
            return false;
        }
    }

    private void handleActionFailure(String action, Object target, Exception e) {
        String description = (target instanceof WebElement) ? getElementDescription((WebElement) target) : target.toString();
        String locatorInfo = (target instanceof WebElement) ? target.toString() : "Locator: " + target;
        
        String errorMessage = String.format("Failed to %s element [%s].<br><b>Locator Detail:</b> %s<br><b>Reason:</b> %s", 
                action, description, locatorInfo, e.getMessage());
        
        // Log4j2 Bridge will automatically push this to ExtentReports as FAIL because it's logger.error
        logger.error(errorMessage);
        
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
