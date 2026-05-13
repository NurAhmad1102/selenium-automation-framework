package org.example.pageObject.frontoffice.login;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class FrontOfficeLoginPage extends BasePage {

    public FrontOfficeLoginPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(name = "username")
    private WebElement usernameField;

    @FindBy(xpath = "//label[contains(text(),'Password')]")
    private WebElement passwordField;

    @FindBy(xpath = "//button[normalize-space()='Sign In']")
    private WebElement signInButton;

    @FindBy(xpath = "//button[contains(., 'Logout')]")
    private WebElement logoutButton;


    public void setUsernameField(String username) {
        typeText(usernameField, username);
    }

    public void setPasswordField(String password) {
        typeText(passwordField, password);
    }

    public void clickSignInButton() {
        signInButton.click();
    }

    public boolean isLogoutButtonVisible() {
       return isVisible(logoutButton);
    }
}
