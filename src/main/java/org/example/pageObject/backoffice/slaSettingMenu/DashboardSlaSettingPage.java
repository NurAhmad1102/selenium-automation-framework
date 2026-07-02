package org.example.pageObject.backoffice.slaSettingMenu;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class DashboardSlaSettingPage extends BasePage {

    public DashboardSlaSettingPage(WebDriver driver){
        super(driver);
        PageFactory.initElements(driver, this);
    }

     //TODO: FIX HERE for XPATH FCKN
    @FindBy(xpath = "//button[span[text()=\"Add\"]]")
    private WebElement addSlaSettingButton;

    @FindBy(xpath = "//h1[text()=\"SLA Setting\"]")
    private WebElement slaSettingPage;

    private final String TD_SLA_PERIOD_NAME = "//td[contains(text(), '%s')]";
    private final String TD_SLA_STATUS = "//td[span[text()= '%s']]";

    public void clickAddSlaSettingButton(){
        click(addSlaSettingButton);
    }

    public boolean isSlaSettingPage(){
        return isVisible(slaSettingPage);
    }

    public List<WebElement> getSlaPeriod(String slaPeriodName){
        String xpath = String.format(TD_SLA_PERIOD_NAME+"/following-sibling::td",slaPeriodName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(xpath)));
    }

    public boolean isSlaSettingDeleted(String slaName){
        String xpath = String.format(TD_SLA_PERIOD_NAME, slaName);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));
        try {
            return wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath(xpath)));
        } catch (TimeoutException e){
            return false;
        }
    }

    public List<WebElement> getSlaByStatus(String status){
        String xpath = String.format(TD_SLA_STATUS, status);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        return wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(By.xpath(xpath)));
    }

}
