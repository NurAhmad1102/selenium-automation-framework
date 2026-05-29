package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.model.OrganizationTestData;
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
}
