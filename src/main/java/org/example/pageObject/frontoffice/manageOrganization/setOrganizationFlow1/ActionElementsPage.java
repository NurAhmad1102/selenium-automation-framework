package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.pagefactory.AjaxElementLocatorFactory;

public class ActionElementsPage extends BasePage {
    public ActionElementsPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//button[svg[contains(@class, 'lucide-arrow-left')]]")
    private WebElement arrowBackButton;

    @FindBy(xpath = "//button[span[text()='Save as Draft']]")
    private WebElement saveAsDraftButton;

    @FindBy(xpath = "//button[text()='Back']")
    private WebElement backButton;

    @FindBy(xpath = "//button[text()='Next']")
    private WebElement nextButton;

    @FindBy(xpath = "//button[svg and contains(@id, 'radix-')]")
    private WebElement kebabButton;



    public void clickArrowBackButton() {
        click(arrowBackButton);
    }

    public void clickSaveAsDraftButton() {
        click(saveAsDraftButton);
    }
    public void clickBackButton() {
        click(backButton);
    }

    public void clickNextButton() {
        click(nextButton);
    }

    public void clickKebabButton() {
        click(kebabButton);
    }

}
