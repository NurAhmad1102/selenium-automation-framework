package org.example.factories.frontoffice.login;

import org.example.pageObject.backoffice.login.BackOfficeLoginPage;
import org.openqa.selenium.WebDriver;

public class FrontOfficeLoginPagesFactory {
    private WebDriver driver;
    private BackOfficeLoginPage backOfficeLoginPage;

    public FrontOfficeLoginPagesFactory(WebDriver driver){
        this.driver = driver;
    }

    /**
     * Lazy init
     * buat object sekali pas awal,
     * akan tetap dipakai selama pakai thread msh idup.
     *
     * @return
     */
    public BackOfficeLoginPage getLoginPage(){
        if(backOfficeLoginPage == null){
            backOfficeLoginPage = new BackOfficeLoginPage(driver);
        }
        return backOfficeLoginPage;
    }
}
