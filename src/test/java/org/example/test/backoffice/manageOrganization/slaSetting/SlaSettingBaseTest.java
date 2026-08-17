package org.example.test.backoffice.manageOrganization.slaSetting;

import org.example.base.BackOfficeBaseTest;
import org.example.drivers.DriverManager;
import org.example.pageObject.backoffice.slaSettingMenu.DashboardSlaSettingPage;
import org.example.pageObject.backoffice.slaSettingMenu.SlaSetting.SlaSettingDetail;
import org.example.pageObject.backoffice.slaSettingMenu.SlaSetting.SlaSettingPage;
import org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal.CopyFromPreviousModal;
import org.example.pageObject.backoffice.slaSettingMenu.modal.addSlaDetailModal.SlaDetailModal;
import org.example.pageObject.backoffice.slaSettingMenu.modal.deleteSlaSetting.DeleteSlaSettingModal;
import org.example.pageObject.backoffice.slaSettingMenu.popUp.AlertPopUpMessage;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.example.utils.account.UserRole;
import org.example.utils.actionUtils.ActionUtils;
import org.example.utils.tableUtils.TableUtils;
import org.example.utils.DevModeUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class SlaSettingBaseTest extends BackOfficeBaseTest {
    private final ThreadLocal<String> loginNip = new ThreadLocal<>();
    private final ThreadLocal<String> username = new ThreadLocal<>();
    private final ThreadLocal<TableUtils> tableUtils = new ThreadLocal<>();
    private final ThreadLocal<SlaSettingHelper> slaSettingHelper = new ThreadLocal<>();
    private final ThreadLocal<ActionUtils> actionUtils = new ThreadLocal<>();

    @Override
    protected String getRole() {
        return UserRole.HR_OD.getValue();
    }

    @BeforeMethod
    public void initSlaSettingContext(org.testng.ITestResult result) {

        String nip = AccountAllocator.getNip();
        String username = AccountAllocator.getUsername();

        this.loginNip.set(nip);
        this.username.set(username);

        goToSlaSetting(nip);

        ActionUtils currentActionUtils = new ActionUtils(DriverManager.getDriver());
        tableUtils.set(new TableUtils(slaSettingPage(), dashboardSlaSettingPage()));
        actionUtils.set(currentActionUtils);
        slaSettingHelper.set(new SlaSettingHelper(getMainPages(), currentActionUtils));
    }

    protected void goToSlaSetting(String loginNip) {
        WebDriver driver = DriverManager.getDriver();
        String currentUrl = driver != null ? driver.getCurrentUrl() : "";

        if (DevModeUtils.isDevMode() && currentUrl.contains("/sla-setting")) {
            logger.info("[DEV-MODE] Active. Browser is already at SLA Setting page (URL: {}). Skipping login and side menu navigation.", currentUrl);
            return;
        }

        if (DevModeUtils.isDevMode() && !currentUrl.contains("login") && !currentUrl.isEmpty() && !currentUrl.equals("about:blank")) {
            logger.info("[DEV-MODE] Active. User is logged in but not on SLA Setting page. Navigating via side menu.");
            sideMenu().getSideMenu().clickManageOrganizationMenu();
            sideMenu().getSideMenu().clickSlaSettingSubMenu();
            dashboardSlaSettingPage().isSlaSettingPage();
            return;
        }

        logger.info("Login Step is Running for user: {}", loginNip);
        String password = AccountAllocator.getPassword();
        if (password == null || password.isEmpty()) {
            password = TestDataFactory.getPassword();
        }

        loginPages().getLoginPage().setUsernameField(loginNip);
        loginPages().getLoginPage().setPasswordField(password);
        loginPages().getLoginPage().clickLoginButton();
        loginPages().getLoginPage().isHeaderWelcomeVisible();
        logger.info("Login successfully with user: {}", loginNip);

        sideMenu().getSideMenu().clickManageOrganizationMenu();
        sideMenu().getSideMenu().clickSlaSettingSubMenu();
        dashboardSlaSettingPage().isSlaSettingPage();
        logger.info("Success into Sla Setting Menu");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanSlaSettingContext() {
        loginNip.remove();
        username.remove();
        tableUtils.remove();
        slaSettingHelper.remove();
        actionUtils.remove();
    }

    protected String loginNip() {
        return loginNip.get();
    }

    protected String username() {
        return username.get();
    }

    protected TableUtils tableUtils() {
        return tableUtils.get();
    }

    protected SlaSettingHelper slaSettingHelper() {
        return slaSettingHelper.get();
    }

    protected ActionUtils actionUtils() {
        return actionUtils.get();
    }

    protected SlaSettingPage slaSettingPage() {
        return getMainPages().manageOrganization()
                .getSlaSettingFactory()
                .getSlaSettingPage();
    }

    protected DashboardSlaSettingPage dashboardSlaSettingPage() {
        return getMainPages().manageOrganization()
                .getDashboardSlaSettingPageFactory()
                .getDashboardSlaSettingPage();
    }

    protected SlaDetailModal slaDetailModal() {
        return getMainPages().manageOrganization()
                .getModalFactory()
                .getAddSlaDetailModalFactory()
                .getSlaDetailModal();
    }

    protected CopyFromPreviousModal copyFromPreviousModal() {
        return getMainPages().manageOrganization()
                .getModalFactory()
                .getAddSlaDetailModalFactory()
                .getCopyFromPreviousModal();
    }

    protected AlertPopUpMessage alertPopUpMessage() {
        return getMainPages().manageOrganization()
                .getPopUpFactory()
                .getAlertPopUpMessage();
    }

    protected SlaSettingDetail slaSettingDetail() {
        return getMainPages().manageOrganization()
                .getSlaSettingFactory()
                .getSlaSettingDetail();
    }

    protected DeleteSlaSettingModal deleteSlaSettingModal() {
        return getMainPages().manageOrganization()
                .getModalFactory()
                .getDeleteSlaSettingModalFactory()
                .getDeleteSlaSettingModal();
    }
}
