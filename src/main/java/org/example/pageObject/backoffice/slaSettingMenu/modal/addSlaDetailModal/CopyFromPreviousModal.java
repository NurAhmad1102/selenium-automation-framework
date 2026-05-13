package org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class CopyFromPreviousModal extends BasePage {
    public CopyFromPreviousModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@class= \"ant-modal-title\" and text()=\"Copy from Previous SLA Setting\"]")
    private WebElement headerCopyFromPrevious;

    @FindBy(xpath = "//div[@class= \"ant-select-selector\"]")
    private WebElement fieldSelectSla;

    @FindBy(xpath = "//button[span[text()=\"Add\"]]")
    private WebElement addButton;


    public void isHeaderCopyFromPrevious(){
        if (!isVisible(headerCopyFromPrevious)) {
            throw new RuntimeException("Copy From Previous header not visible");
        }
    }

    public void clickFieldSelectSla(){
        click(fieldSelectSla);
    }

    public void clickAddButton(){
        click(addButton);
    }



//    public void selectSlaSetting(String slaSettingName){
//        String xpath = "//div[contains(@class,'ant-select-dropdown')]";
//        ActionUtils actionUtils = new ActionUtils(driver);
//        actionUtils.selectElementInDropdownByText(xpath, slaSettingName);
//    }


}
