package org.example.pageObject.backoffice.slaSettingMenu.slasetting;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class SlaSettingDetail extends BasePage {

    public SlaSettingDetail(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//p[text()=\"SLA Setting Detail\"]")
    private WebElement slaSettingDetailHeader;

    public void isSlaSettingDetailHeader(){
        if (!isVisible(slaSettingDetailHeader)) {
            throw new RuntimeException("SLA Setting Detail header not visible");
        }
    }


}
