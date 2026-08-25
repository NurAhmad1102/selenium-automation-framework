package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1;

import org.example.pageObject.base.BasePage;
import org.example.pageObject.frontoffice.manageOrganization.enums.OrgMenuAction;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class SetOrganizationPage extends BasePage {

    public SetOrganizationPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//span[text()='Set Organization']")
    private WebElement setOrganizationHeader;

    private final By MissingParentPositionNode = By.xpath("//div[@data-orientation='vertical'][.//span[normalize-space()='Missing Parent Position']]");

    public boolean isSetOrganizationHeaderDisplayed() {
        return isVisible(setOrganizationHeader);
    }

    public void clickNodeOrganizationFlow1(String organizationName){
        String xpath = String.format("//span[text()='%s']/ancestor::div[@data-orientation='vertical']", organizationName);
        WebElement element = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        click(element);
    }

    public void clickKebabButtonOfNodeOrganizationFlow1(String organizationName){
        String xpath = String.format("//span[text()='%s']/ancestor::div[@data-orientation='vertical']//button[@type='button']", organizationName);
        WebElement kebabBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpath)));
        click(kebabBtn);
    }

    public String getNodeText(String orgName) {
        String xpath = String.format("//span[text()='%s']", orgName);
        WebElement textElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath(xpath)));
        return textElement.getText();
    }

    public boolean isNodeOrganizationDisplayed(String orgName) {
        String xpath = String.format("//span[text()='%s']", orgName);
        return isVisible(By.xpath(xpath));
    }

    public boolean isNodeOrganizationInvisible(String orgName) {
        String xpath = String.format("//span[text()='%s']", orgName);
        return isNotVisible(By.xpath(xpath));
    }

    public void selectMenuActionFlow1(OrgMenuAction orgMenuAction){
        String xpathExpression = String.format("//div[@role='menuitem' and normalize-space()='%s']", orgMenuAction.getActionName());
        WebElement menuItem = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(xpathExpression)));
        click(menuItem);
    }

    public boolean isMissingParentPositionNodeDisplayed() {
//        return isVisible(MissingParentPositionNode);
        return !driver.findElements(MissingParentPositionNode).isEmpty();
    }

    public void clickNodeMissingParentPosition() {
        click(MissingParentPositionNode);
    }

}
