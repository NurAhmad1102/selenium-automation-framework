package org.example.base;

import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.utils.account.UserRole;
import org.example.utils.config.UrlProvider;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class FrontOfficeBaseTest extends BaseTest {

    @Override
    protected String getUrl() {
        String excelUrl = UrlProvider.getFrontOfficeUrl();
        if (excelUrl == null || excelUrl.isEmpty()) {
            throw new RuntimeException("URL Frontoffice tidak ditemukan! Pastikan interface 'Frontoffice' terisi di sheet URL.");
        }
        return excelUrl;
    }

    @Override
    @BeforeMethod
    public void setUp(org.testng.ITestResult result) {
        super.setUp(result);
        logger.info("Initializing Front Office page factories");
        frontOfficePages();
    }

    @Override
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        try {
            logger.info("Front Office page factory instance removed successfully");
        } finally {
            super.tearDown(result);
        }
    }

//
    protected FrontOfficeMainPagesFactory getFrontOfficePages() {
        FrontOfficeMainPagesFactory pages = frontOfficePages();
        if (pages == null) {
            throw new IllegalStateException("Front Office page factories are not initialized. Ensure setUp() runs before accessing factories.");
        }
        return pages;
    }

    protected void useFrontOfficeUser(String alias, UserRole role) {
        super.useFrontOfficeUser(alias, role);
    }

    protected void useFrontOfficeUser(String alias, String role) {
        super.useFrontOfficeUser(alias, role);
    }

    protected void useFrontOfficeUser(String alias) {
        super.useFrontOfficeUser(alias);
    }

    protected void loginFrontOfficeCurrentUser() {
        officeSession().loginFrontOfficeCurrentUser();
    }
}
