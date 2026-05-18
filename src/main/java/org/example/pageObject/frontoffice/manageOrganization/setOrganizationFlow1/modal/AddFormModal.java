package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.openqa.selenium.WebDriver;

public class AddFormModal extends BaseFormModal {

    public AddFormModal(WebDriver driver) {
        super(driver, "Add Organization");
    }

    public boolean isAddFormHeaderVisible(){
        return isModalHeaderDisplayed();
    }



}
