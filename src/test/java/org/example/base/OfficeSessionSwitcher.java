package org.example.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.config.userLoginStructure.UserloginConfig;
import org.example.drivers.DriverManager;
import org.example.mainPagesFactory.BackOfficeMainPagesFactory;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.example.utils.account.UserRole;
import org.example.utils.config.UrlProvider;
import org.example.utils.report.core.ReportAPI;
import org.example.utils.DevModeUtils;
import org.openqa.selenium.WebDriver;

public class OfficeSessionSwitcher {
    private final Logger logger = LogManager.getLogger(getClass());
    private final BaseTest owner;
    private BackOfficeMainPagesFactory backOfficePages;
    private FrontOfficeMainPagesFactory frontOfficePages;

    OfficeSessionSwitcher(BaseTest owner) {
        this.owner = owner;
    }

    public BackOfficeMainPagesFactory backOfficePages() {
        if (backOfficePages == null) {
            backOfficePages = new BackOfficeMainPagesFactory(currentDriver());
        }
        return backOfficePages;
    }

    public FrontOfficeMainPagesFactory frontOfficePages() {
        if (frontOfficePages == null) {
            frontOfficePages = new FrontOfficeMainPagesFactory(currentDriver());
        }
        return frontOfficePages;
    }

    public void useBackOfficeUser(String alias, UserRole role) {
        useBackOfficeUser(alias, role.getValue());
    }

    public void useBackOfficeUser(String alias, String role) {
        AccountAllocator.useAccount(alias, owner.currentTestName(), role);
        loginBackOfficeCurrentUser();
    }

    public void useBackOfficeUser(String alias) {
        AccountAllocator.useAccount(alias);
        loginBackOfficeCurrentUser();
    }

    public void useFrontOfficeUser(String alias, UserRole role) {
        useFrontOfficeUser(alias, role.getValue());
    }

    public void useFrontOfficeUser(String alias, String role) {
        AccountAllocator.useAccount(alias, owner.currentTestName(), role);
        loginFrontOfficeCurrentUser();
    }

    public void useFrontOfficeUser(String alias) {
        AccountAllocator.useAccount(alias);
        loginFrontOfficeCurrentUser();
    }

    public void loginBackOfficeCurrentUser() {
        UserloginConfig account = owner.currentAccount();
        ReportAPI.step("Switch Back Office user to alias: " + owner.currentAccountAlias()
                + " role: " + account.getRole());

        WebDriver driver = DriverManager.getDriver();
        String currentUrl = driver != null ? driver.getCurrentUrl() : "";
        if (DevModeUtils.isDevMode() && (currentUrl.contains("backoffice") || currentUrl.contains("dashboard") || (!currentUrl.contains("login") && !currentUrl.equals("about:blank") && !currentUrl.isEmpty()))) {
            logger.info("[DEV-MODE] Active. Already logged in or on Back Office page (URL: {}). Skipping login.", currentUrl);
            return;
        }

        owner.resetBrowserSession();
        DriverManager.getDriver().get(requireUrl(UrlProvider.getBackOfficeUrl(), "Back Office"));

        String password = account.getPassword();
        if (password == null || password.isEmpty()) {
            password = TestDataFactory.getPassword();
        }

        backOfficePages().login().getLoginPage().setUsernameField(account.getNip());
        backOfficePages().login().getLoginPage().setPasswordField(password);
        backOfficePages().login().getLoginPage().clickLoginButton();
        backOfficePages().login().getLoginPage().isHeaderWelcomeVisible();
        logger.info("Back Office user switched successfully: [Alias: {}, Role: {}, NIP: {}]",
                owner.currentAccountAlias(), account.getRole(), account.getNip());
    }

    public void loginFrontOfficeCurrentUser() {
        UserloginConfig account = owner.currentAccount();
        ReportAPI.step("Switch Front Office user to alias: " + owner.currentAccountAlias()
                + " role: " + account.getRole());

        WebDriver driver = DriverManager.getDriver();
        String currentUrl = driver != null ? driver.getCurrentUrl() : "";
        if (DevModeUtils.isDevMode() && (currentUrl.contains("frontoffice") || currentUrl.contains("dashboard") || (!currentUrl.contains("login") && !currentUrl.equals("about:blank") && !currentUrl.isEmpty()))) {
            logger.info("[DEV-MODE] Active. Already logged in or on Front Office page (URL: {}). Skipping login.", currentUrl);
            return;
        }

        owner.resetBrowserSession();
        DriverManager.getDriver().get(requireUrl(UrlProvider.getFrontOfficeUrl(), "Front Office"));

        String password = account.getPassword();
        if (password == null || password.isEmpty()) {
            password = TestDataFactory.getPassword();
        }

        frontOfficePages().login().getLoginPage().setUsernameField(account.getNip());
        frontOfficePages().login().getLoginPage().setPasswordField(password);
        frontOfficePages().login().getLoginPage().clickSignInButton();
        if (!frontOfficePages().login().getLoginPage().isLogoutButtonVisible()) {
            throw new RuntimeException("Front Office login failed for alias: " + owner.currentAccountAlias());
        }
        logger.info("Front Office user switched successfully: [Alias: {}, Role: {}, NIP: {}]",
                owner.currentAccountAlias(), account.getRole(), account.getNip());
    }

    void clear() {
        backOfficePages = null;
        frontOfficePages = null;
    }

    private String requireUrl(String url, String officeName) {
        if (url == null || url.isEmpty()) {
            throw new RuntimeException("URL " + officeName + " tidak ditemukan! Pastikan sheet URL terisi.");
        }
        return url;
    }

    private WebDriver currentDriver() {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is not initialized. Ensure setUp() runs before accessing office pages.");
        }
        return driver;
    }
}
