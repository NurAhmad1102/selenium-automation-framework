package org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal;

import org.openqa.selenium.WebDriver;

public class SplitFormModal extends BaseFormModal {
    public SplitFormModal(WebDriver driver) {
        super(driver, "Split Organization");
     }

    public boolean isSplitFormHeaderVisible(){
        return isModalHeaderDisplayed();
    }
}
