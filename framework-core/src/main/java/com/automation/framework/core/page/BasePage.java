package com.automation.framework.core.page;

import com.automation.framework.core.config.FrameworkConfig;
import com.automation.framework.core.report.core.ElementSnapshotInspector;
import com.automation.framework.core.report.core.ReportAPI;
import com.automation.framework.core.report.internal.model.ElementSnapshot;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

/**
 * Agnostic foundational Page Object class providing safe interaction wrappers,
 * dynamic waits, JavaScript fallbacks, and automatic diagnostic logging.
 */
public abstract class BasePage {

    protected final WebDriver driver;
    protected final WebDriverWait wait;
    protected final int defaultTimeout;
    protected final Logger logger = LogManager.getLogger(getClass());

    private static final ThreadLocal<String> lastInteractedLocator = new ThreadLocal<>();

    public BasePage(WebDriver driver) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver must not be null!");
        }
        this.driver = driver;
        this.defaultTimeout = FrameworkConfig.getDefaultTimeout();
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(this.defaultTimeout));
    }

    public BasePage(WebDriver driver, int timeoutInSeconds) {
        if (driver == null) {
            throw new IllegalArgumentException("WebDriver must not be null!");
        }
        this.driver = driver;
        this.defaultTimeout = timeoutInSeconds;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(timeoutInSeconds));
    }

    public static String getLastInteractedLocator() {
        return lastInteractedLocator.get();
    }

    public static void setLastInteractedLocator(Object target) {
        if (target != null) {
            String str = target.toString().trim();
            int arrowIdx = str.indexOf("-> ");
            if (arrowIdx >= 0) {
                str = str.substring(arrowIdx + 3);
                if (str.endsWith("]")) {
                    str = str.substring(0, str.length() - 1).trim();
                }
            }
            lastInteractedLocator.set(str);
        }
    }

    protected void click(WebElement element) {
        setLastInteractedLocator(element);
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

    protected void click(By locator) {
        setLastInteractedLocator(locator);
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

    protected void clickWithJS(By locator) {
        setLastInteractedLocator(locator);
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

    protected void typeText(WebElement element, String text) {
        setLastInteractedLocator(element);
        String description = getElementDescription(element);
        try {
            logger.info("Typing '{}' into element: {}", text, description);
            wait.until(ExpectedConditions.visibilityOf(element)).clear();
            element.sendKeys(text);
            ReportAPI.step("Action: Type text '" + text + "' into element [" + description + "]");
        } catch (Exception e) {
            handleActionFailure("type text", element, e);
        }
    }

    protected String getAttributeValue(WebElement element, String attribute) {
        setLastInteractedLocator(element);
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
        setLastInteractedLocator(element);
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
        setLastInteractedLocator(locator);
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

    protected boolean isVisible(WebElement element) {
        setLastInteractedLocator(element);
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
        setLastInteractedLocator(locator);
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

    protected boolean isNotVisible(WebElement element) {
        setLastInteractedLocator(element);
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
        setLastInteractedLocator(locator);
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

    protected void selectDropdownModal(WebElement element, String option) {
        String description = getElementDescription(element);
        logger.info("Selecting option [{}] from dropdown [{}]", option, description);
        ReportAPI.step("Action: Select option [" + option + "] from dropdown [" + description + "]");
        click(element);

        DropdownComponent dropdownComponent = new DropdownComponent(driver, wait);
        if (dropdownComponent.isSearchableDropdown()) {
            dropdownComponent.selectSearchableDropdown(option);
        } else {
            dropdownComponent.selectScrollableDropdown(option);
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

    private void handleActionFailure(String action, Object target, Exception e) {
        String description = (target instanceof WebElement) ? getElementDescription((WebElement) target) : target.toString();
        String locatorInfo = (target instanceof WebElement) ? target.toString() : "Locator: " + target;

        String logConsoleMessage = String.format("Failed to %s element [%s]. Locator Detail: %s. Reason: %s",
                action, description, locatorInfo, e.getMessage());

        logger.error(logConsoleMessage);

        ElementSnapshot snapshot = null;
        try {
            snapshot = ElementSnapshotInspector.captureSnapshot(
                    driver, target, action, e, Duration.ofSeconds(defaultTimeout), null);
        } catch (Exception ignored) {
        }

        ReportAPI.elementError(action, locatorInfo, e, snapshot);

        throw new RuntimeException(logConsoleMessage, e);
    }

    private String sanitizeDescription(String input) {
        if (input == null) return "";
        String trimmed = input.trim().replaceAll("\\s+", " ");
        return trimmed.length() > 50 ? trimmed.substring(0, 47) + "..." : trimmed;
    }

    private boolean isNotBlank(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
