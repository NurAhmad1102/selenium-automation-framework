package org.example.base;

import org.example.drivers.DriverManager;
import org.example.factories.frontoffice.login.FrontOfficeLoginPagesFactory;
import org.example.factories.frontoffice.sideMenuFactory.FrontOfficeSideMenuFactory;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.utils.account.AccountAllocator;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class FrontOfficeBaseTest extends BaseTest {

    private final ThreadLocal<FrontOfficeMainPagesFactory> mainPages = new ThreadLocal<>();

    @Override
    protected String getUrl() {
        String excelUrl = AccountAllocator.getUrl("Frontoffice_staging");
        if (excelUrl == null || excelUrl.isEmpty()) {
            throw new RuntimeException("URL Frontoffice tidak ditemukan! Pastikan interface 'Frontoffice' terisi di sheet URL.");
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
        logger.info("Initializing Front Office page factories");
        mainPages.set(new FrontOfficeMainPagesFactory(DriverManager.getDriver()));
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            mainPages.remove();
            logger.info("Front Office page factory instance removed successfully");
        } finally {
            super.tearDown(result);
        }
    }


//
    protected FrontOfficeMainPagesFactory getFrontOfficePages() {
        FrontOfficeMainPagesFactory pages = mainPages.get();
        if (pages == null) {
            throw new IllegalStateException("Front Office page factories are not initialized. Ensure setUp() runs before accessing factories.");
        }
        return pages;
    }
}
