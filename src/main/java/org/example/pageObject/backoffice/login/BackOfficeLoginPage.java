package org.example.pageObject.backoffice.login;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class BackOfficeLoginPage extends BasePage {

    public BackOfficeLoginPage(WebDriver driver){
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(name = "username")
    private WebElement usernameField;

    @FindBy(name = "password")
    private WebElement passwordField;

    @FindBy(xpath = "//button[text()='Login']")
    private WebElement loginButton;

    @FindBy(xpath = "//h2[contains(text(), 'Welcome')]")
    private WebElement headerWelcome;

    public void setUsernameField(String username){
        typeText(usernameField, username);
    }

    public void setPasswordField(String password){
        typeText(passwordField, password);
    }

    public void clickLoginButton(){
        click(loginButton);
    }

    public boolean isHeaderWelcomeVisible(){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        return wait.until(ExpectedConditions.visibilityOf(headerWelcome)).isDisplayed();
    }
}


