package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.example.base.FrontOfficeBaseTest;
import org.example.drivers.DriverManager;
import org.example.enums.frontoffice.FrontOfficeMenu;
import org.example.factories.frontoffice.manageOrganization.SetOrganizationModalFactory;
import org.example.pageObject.frontoffice.manageOrganization.DashboardManageOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.FilterUpdateOrganizationPage;
import org.example.pageObject.frontoffice.manageOrganization.setOrganizationFlow1.SetOrganizationPage;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.example.utils.account.UserRole;
import org.example.utils.actionUtils.ActionUtils;
import org.example.utils.DevModeUtils;
import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class ManageOrganizationBaseTest extends FrontOfficeBaseTest {

    private final ThreadLocal<String> loginNip = new ThreadLocal<>();
    private final ThreadLocal<String> username = new ThreadLocal<>();
    private final ThreadLocal<ManageOrganizationHelper> manageOrganizationHelper = new ThreadLocal<>();
    private final ThreadLocal<SetOrganizationHelper> setOrganizationHelper = new ThreadLocal<>();
    private final ThreadLocal<AssignEmployeeHelper> assignEmployeeHelper = new ThreadLocal<>();
    private final ThreadLocal<OrganizationChangesJustificationHelper> justificationHelper = new ThreadLocal<>();
    private final ThreadLocal<ActionUtils> actionUtils = new ThreadLocal<>();

    @Override
    protected String getRole() {
        return UserRole.HR_OD.getValue();
    }

    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result){

        String nip = AccountAllocator.getNip();
        String username = AccountAllocator.getUsername();

        this.loginNip.set(nip);
        this.username.set(username);

        goToManageOrganization(nip);

        setOrganizationHelper.set(new SetOrganizationHelper(getFrontOfficePages()));
        manageOrganizationHelper.set(new ManageOrganizationHelper(getFrontOfficePages(), setOrganizationHelper.get()));
        assignEmployeeHelper.set(new AssignEmployeeHelper(getFrontOfficePages(), manageOrganizationHelper.get(), setOrganizationHelper.get()));
        justificationHelper.set(new OrganizationChangesJustificationHelper(getFrontOfficePages(), manageOrganizationHelper.get()));
        actionUtils.set(new ActionUtils(DriverManager.getDriver()));
    }

    protected void goToManageOrganization(String loginNip){
        WebDriver driver = DriverManager.getDriver();
        String currentUrl = driver != null ? driver.getCurrentUrl() : "";

        if (DevModeUtils.isDevMode() && (currentUrl.contains("manage-organization") || currentUrl.contains("my-organization"))) {
            logger.info("[DEV-MODE] Active. Browser is already at Manage Organization page (URL: {}). Skipping login and side menu navigation.", currentUrl);
            return;
        }

        if (DevModeUtils.isDevMode() && !currentUrl.contains("login") && !currentUrl.isEmpty() && !currentUrl.equals("about:blank")) {
            logger.info("[DEV-MODE] Active. User is logged in but not on Manage Organization page. Navigating via side menu.");
            getFrontOfficePages().sideMenu().getFrontOfficeSideMenu().expandMainMenu(FrontOfficeMenu.MY_ORGANIZATION);
            getFrontOfficePages().sideMenu().getFrontOfficeSideMenu().clickSubMenu(FrontOfficeMenu.MANAGE_ORGANIZATION);
            if (!dashboardManageOrganizationPage().isManageOrganizationHeaderDisplayed()) {
                throw new RuntimeException("Manage Organization page header not visible");
            }
            return;
        }

        logger.info("Login Step is Running for user: {}", loginNip);
        String password = AccountAllocator.getPassword();
        if(password == null || password.isEmpty()){
            password = TestDataFactory.getPassword();
        }

        getFrontOfficePages().login().getLoginPage().setUsernameField(loginNip);
        getFrontOfficePages().login().getLoginPage().setPasswordField(password);
        getFrontOfficePages().login().getLoginPage().clickSignInButton();

        logger.info("Navigating to Manage Organization page");
        getFrontOfficePages().sideMenu().getFrontOfficeSideMenu().closeModalIfVisible(); //jika ada modal blocking
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
        assignEmployeeHelper.remove();
        justificationHelper.remove();
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

    protected AssignEmployeeHelper assignEmployeeHelper() {
        return assignEmployeeHelper.get();
    }

    protected OrganizationChangesJustificationHelper justificationHelper() {
        return justificationHelper.get();
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


    //    TODO: ganti data ini nanti, selama dev biarkan dulu
    protected String duplicateOrganizationName = "Special Asset ID";
    protected String duplicatePositionName = "Head of Foreclosed Asset & Business Planning";

    protected String duplicateOrganizationMessage = "Organization name already exists";
    protected String duplicateOrganizationNameInBothOrg = "Organization name is duplicated within split organizations";
    protected String duplicatePositionMessage = "Position name already exists";
    protected String splitDuplicatePositionInBothOrg = "Position name must be unique across organizations";
}
