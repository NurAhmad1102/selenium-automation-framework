package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.example.base.FrontOfficeBaseTest;
import org.example.drivers.DriverManager;
import org.example.enums.frontoffice.FrontOfficeMenu;
import org.example.factories.frontoffice.manageOrganization.SetOrganizationModalFactory;
import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.FilterUpdateOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.AddJobPositionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.ConfirmationActionModal;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.modal.SuccessModal;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.example.utils.actionUtils.ActionUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class ManageOrganizationBaseTest extends FrontOfficeBaseTest {

    private final ThreadLocal<String> loginNip = new ThreadLocal<>();
    private final ThreadLocal<String> username = new ThreadLocal<>();
    private final ThreadLocal<ManageOrganizationHelper> manageOrganizationHelper = new ThreadLocal<>();
    private final ThreadLocal<SetOrganizationHelper> setOrganizationHelper = new ThreadLocal<>();
    private final ThreadLocal<ActionUtils> actionUtils = new ThreadLocal<>();

    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result){

        String nip = AccountAllocator.getNip();
        String username = AccountAllocator.getUsername();

        this.loginNip.set(nip);
        this.username.set(username);

        goToManageOrganization(nip);

        manageOrganizationHelper.set(new ManageOrganizationHelper(getFrontOfficePages()));
        setOrganizationHelper.set(new SetOrganizationHelper(getFrontOfficePages()));
        actionUtils.set(new ActionUtils(DriverManager.getDriver()));
    }

    protected void goToManageOrganization(String loginNip){
        logger.info("Login Step is Running for user: {}", loginNip);
        String password = AccountAllocator.getPassword();
        if(password == null || password.isEmpty()){
            password = TestDataFactory.getPassword();
        }

        getFrontOfficePages().login().getLoginPage().setUsernameField(loginNip);
        getFrontOfficePages().login().getLoginPage().setPasswordField(password);
        getFrontOfficePages().login().getLoginPage().clickSignInButton();

        logger.info("Navigating to Manage Organization page");
        getFrontOfficePages().sideMenu().getFrontOfficeSideMenu().expandMainMenu(FrontOfficeMenu.MY_ORGANIZATION);
        getFrontOfficePages().sideMenu().getFrontOfficeSideMenu().clickSubMenu(FrontOfficeMenu.MANAGE_ORGANIZATION);
        if (!dashboardManageOrganizationPage().isManageOrganizationHeaderDisplayed()) {
            throw new RuntimeException("Manage Organization page header not visible");
        }

        logger.info("Success into Manage Organization Menu");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanManageOrganizationContext(){
        loginNip.remove();
        username.remove();
        manageOrganizationHelper.remove();
        setOrganizationHelper.remove();
        actionUtils.remove();
    }

    protected DashboardManageOrganizationPage dashboardManageOrganizationPage() {
        return getFrontOfficePages().manageOrganization().getDashboardManageOrganizationPage();
    }

    protected FilterUpdateOrganizationPage filterUpdateOrganizationPage() {
        return getFrontOfficePages().manageOrganization().getFilterUpdateOrganizationPage();
    }

    protected SetOrganizationPage setOrganizationFlow1Page() {
        return getFrontOfficePages().manageOrganization().getSetOrganizationFlow1Page();
    }

    protected SetOrganizationModalFactory setOrganizationModals() {
        return getFrontOfficePages().manageOrganization().modals();
    }

    protected ManageOrganizationHelper manageOrganizationHelper() {
        return manageOrganizationHelper.get();
    }

    protected SetOrganizationHelper setOrganizationHelper() {
        return setOrganizationHelper.get();
    }

    protected String loginNip() {
        return loginNip.get();
    }

    protected String username() {
        return username.get();
    }

    protected ActionUtils actionUtils() {
        return actionUtils.get();
    }
}
