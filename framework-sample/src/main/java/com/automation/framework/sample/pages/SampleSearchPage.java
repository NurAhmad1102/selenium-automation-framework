package com.automation.framework.sample.pages;

import com.automation.framework.core.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/**
 * Sample Page Object representing a generic search page.
 */
public class SampleSearchPage extends BasePage {

    private final By searchInput = By.name("q");
    private final By searchResultsContainer = By.id("search");

    public SampleSearchPage(WebDriver driver) {
        super(driver);
    }

    public void enterSearchQuery(String query) {
        typeText(searchInput, query);
    }

    public boolean isSearchInputDisplayed() {
        return isVisible(searchInput);
    }
}
