package org.example.base;

import org.example.factories.backoffice.login.BackOfficeLoginPagesFactory;
import org.example.factories.backoffice.sideMenu.SideMenuFactory;
import org.example.mainPagesFactory.BackOfficeMainPagesFactory;
import org.example.utils.account.UserRole;
import org.example.utils.config.UrlProvider;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BackOfficeBaseTest extends BaseTest {

    @Override
    protected String getUrl() {
        String excelUrl = UrlProvider.getBackOfficeUrl();
        if (excelUrl == null || excelUrl.isEmpty()) {
            throw new RuntimeException(
                    "URL Backoffice tidak ditemukan! Pastikan interface 'Backoffice' terisi di sheet URL.");
        }
        return excelUrl;
    }

    @Override
    @BeforeMethod
    public void setUp(org.testng.ITestResult result) {
        super.setUp(result);
        logger.info("Initializing Back Office page factories");
        backOfficePages();
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            logger.info("Back Office page factory instance removed successfully");
        } finally {
            super.tearDown(result);
        }
    }

    // TAMBAH DARI FACTORIES FRONT OFFICE
    @Deprecated
    protected BackOfficeLoginPagesFactory loginPages() {
        return getMainPages().login();
    }

    @Deprecated
    protected SideMenuFactory sideMenu() {
        return getMainPages().sideMenu();
    }

    protected void useBackOfficeUser(String alias, UserRole role) {
        super.useBackOfficeUser(alias, role);
    }

    protected void useBackOfficeUser(String alias, String role) {
        super.useBackOfficeUser(alias, role);
    }

    protected void useBackOfficeUser(String alias) {
        super.useBackOfficeUser(alias);
    }

    protected void loginBackOfficeCurrentUser() {
        officeSession().loginBackOfficeCurrentUser();
    }


    //
    protected BackOfficeMainPagesFactory getMainPages() {
        BackOfficeMainPagesFactory pages = backOfficePages();
        if (pages == null) {
            throw new IllegalStateException(
                    "Back Office page factories are not initialized. Ensure setUp() runs before accessing factories.");
        }
        return pages;
    }
}
