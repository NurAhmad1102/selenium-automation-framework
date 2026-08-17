package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.actionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.BaseFormModal;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

public class RenameFormModal extends BaseFormModal {

    public RenameFormModal(WebDriver driver) {
        super(driver,"Rename Organization");
        PageFactory.initElements(driver, this);
    }

    public boolean isRenameFormHeaderVisible(){
        return isModalHeaderDisplayed();
    }

    public boolean isRenameJobPositionVisible(){
        try {
            String xpath = String.format("//h2[normalize-space()='Rename Job & Position']");
            return isVisible(By.xpath(xpath));
        } catch (Exception e) {
            return false;
        }
    }
}
