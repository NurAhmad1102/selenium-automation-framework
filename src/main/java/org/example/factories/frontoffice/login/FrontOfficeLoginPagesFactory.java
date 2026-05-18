package org.example.factories.frontoffice.login;

import org.example.pageObject.frontoffice.login.FrontOfficeLoginPage;
import org.openqa.selenium.WebDriver;

public class FrontOfficeLoginPagesFactory {
    private WebDriver driver;
    private FrontOfficeLoginPage frontOfficeLoginPage;

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
    public FrontOfficeLoginPage getLoginPage(){
        if(frontOfficeLoginPage == null){
            frontOfficeLoginPage = new FrontOfficeLoginPage(driver);
        }
        return frontOfficeLoginPage;
    }
}
