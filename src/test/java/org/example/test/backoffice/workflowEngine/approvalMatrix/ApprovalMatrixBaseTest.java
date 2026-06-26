package org.example.test.backoffice.workflowEngine.approvalMatrix;

import org.example.base.BackOfficeBaseTest;
import org.example.drivers.DriverManager;
import org.example.test.backoffice.manageOrganization.slaSetting.SlaSettingHelper;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.example.utils.account.UserRole;
import org.example.utils.actionUtils.ActionUtils;
import org.example.utils.tableUtils.TableUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class ApprovalMatrixBaseTest extends BackOfficeBaseTest {
    private final ThreadLocal<String> loginNip = new ThreadLocal<>();
    private final ThreadLocal<String> username = new ThreadLocal<>();
    private final ThreadLocal<ActionUtils> actionUtils = new ThreadLocal<>();

    @Override
    protected String getRole() {
        return UserRole.HR_OD.getValue();
    }

    @BeforeMethod
    public void initApprovalMatrixContext(org.testng.ITestResult result) {

        String nip = AccountAllocator.getNip();
        String username = AccountAllocator.getUsername();

        this.loginNip.set(nip);
        this.username.set(username);

        goToApprovalMatrix(nip);

//        tableUtils.set(new TableUtils(DriverManager.getDriver()));
//        actionUtils.set(new ActionUtils(DriverManager.getDriver()));
        actionUtils.set(new ActionUtils(DriverManager.getDriver()));
    }

    protected void goToApprovalMatrix(String loginNip){
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
//        dashboardSlaSettingPage().isSlaSettingPage();
        logger.info("Success into Sla Setting Menu");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanSlaSettingContext() {
        loginNip.remove();
        username.remove();
        actionUtils.remove();
    }

    protected String loginNip() {
        return loginNip.get();
    }

    protected String username() {
        return username.get();
    }
}
