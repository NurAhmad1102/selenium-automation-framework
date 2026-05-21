package org.example.base;

import org.example.factories.backoffice.login.BackOfficeLoginPagesFactory;
import org.example.factories.backoffice.sideMenu.SideMenuFactory;
import org.example.drivers.DriverManager;
import org.example.mainPagesFactory.BackOfficeMainPagesFactory;
import org.example.utils.account.AccountAllocator;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class BackOfficeBaseTest extends BaseTest {

    private final ThreadLocal<BackOfficeMainPagesFactory> mainPages = new ThreadLocal<>();

    @Override
    protected String getUrl() {
        String excelUrl = AccountAllocator.getUrl("Backoffice_staging");
        if (excelUrl == null || excelUrl.isEmpty()) {
            throw new RuntimeException(
                    "URL Backoffice tidak ditemukan! Pastikan interface 'Backoffice' terisi di sheet URL.");
        }
        return excelUrl;
    }

    @Override
    protected String getRole() {
        return "HR OD";
    }

    @Override
    @BeforeMethod
    public void setUp(org.testng.ITestResult result) {
        super.setUp(result);
        logger.info("Initializing Back Office page factories");
        mainPages.set(new BackOfficeMainPagesFactory(DriverManager.getDriver()));
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            mainPages.remove();
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


    //
    protected BackOfficeMainPagesFactory getMainPages() {
        BackOfficeMainPagesFactory pages = mainPages.get();
        if (pages == null) {
            throw new IllegalStateException(
                    "Back Office page factories are not initialized. Ensure setUp() runs before accessing factories.");
        }
        return pages;
    }
}
