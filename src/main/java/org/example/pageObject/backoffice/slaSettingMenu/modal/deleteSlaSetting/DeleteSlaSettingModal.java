package org.example.pageObject.backoffice.slaSettingMenu.modal.deleteSlaSetting;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DeleteSlaSettingModal extends BasePage {
    public DeleteSlaSettingModal(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    @FindBy(xpath = "//div[@class = \"ant-modal-confirm-body\"]/span[text()=\"Are you sure you want to delete this SLA?\"]")
    private WebElement popUpConfirmDelete;

    @FindBy(xpath = "//button[contains(@class, \"ant-btn-dangerous\")]/span[text()=\"OK\"]")
    private WebElement okDeleteButton;

    @FindBy(xpath = "//div[contains(@class, 'ant-message-success')]//span[contains(text(), 'SLA has been deleted successfully.')]")
    private WebElement popUpDeleteSuccess;

    public void isConfirmDelete(){
        if (!isVisible(popUpConfirmDelete)) {
            throw new RuntimeException("Confirmation delete pop-up not visible");
        }
    }

    public void clickOkDeleteButton(){
        click(okDeleteButton);
    }

    public void isPopUpDeleteSuccess(){
        if (!isVisible(popUpDeleteSuccess)) {
            throw new RuntimeException("Delete success pop-up not visible");
        }
    }

}
