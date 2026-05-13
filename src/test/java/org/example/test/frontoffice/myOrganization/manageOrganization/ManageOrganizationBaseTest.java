package org.example.test.frontoffice.myOrganization.manageOrganization;

import org.example.base.BaseTest;
import org.example.base.FrontOfficeBaseTest;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.example.utils.actionUtils.ActionUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class ManageOrganizationBaseTest extends FrontOfficeBaseTest {

    private final ThreadLocal<String> loginNip = new ThreadLocal<>();
    private final ThreadLocal<String> username = new ThreadLocal<>();
    private final ThreadLocal<ManageOrganizationHelper> manageOrganizationHelper = new ThreadLocal<>();
    private final ThreadLocal<ActionUtils> actionUtils = new ThreadLocal<>();

    @BeforeMethod
    public void initManageOrganizationContext(org.testng.ITestResult result){

        String nip = AccountAllocator.getNip();
        String username = AccountAllocator.getUsername();

        this.loginNip.set(nip);
        this.username.set(username);



    }

    protected void goToManageOrganization(String loginNip){
        logger.info("Login Step is Running for user: {}", loginNip);
        String password = AccountAllocator.getPassword();
        if(password == null || password.isEmpty()){
            password = TestDataFactory.getPassword();
        }


        logger.info("Success into Manage Organization Menu");
    }

    @AfterMethod
    public void cleanManageOrganizationContext(){
        loginNip.remove();
        username.remove();
        manageOrganizationHelper.remove();
        actionUtils.remove();
    }


}
