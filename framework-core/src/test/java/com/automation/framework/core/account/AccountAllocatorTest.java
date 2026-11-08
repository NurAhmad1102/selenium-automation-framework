package com.automation.framework.core.account;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class AccountAllocatorTest {

    private static class TestUser implements UserAccount {
        private final String username;
        private final String password;
        private final String role;

        TestUser(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }

        @Override public String getUsername() { return username; }
        @Override public String getPassword() { return password; }
        @Override public String getRole() { return role; }
    }

    @BeforeMethod
    public void setUp() {
        AccountAllocator.clearPool();
        AccountAllocator.registerAccounts(List.of(
                new TestUser("admin1", "pass1", "Admin"),
                new TestUser("admin2", "pass2", "Admin"),
                new TestUser("user1", "pass3", "Standard")
        ));
    }

    @AfterMethod
    public void tearDown() {
        AccountAllocator.releaseAllAccounts();
        AccountAllocator.clearPool();
    }

    @Test
    public void testAllocateAccount() {
        AccountAllocator.allocateAccount("test1", "Admin");
        UserAccount current = AccountAllocator.getCurrentAccount();

        Assert.assertNotNull(current);
        Assert.assertEquals(current.getRole(), "Admin");
        Assert.assertTrue(current.getUsername().startsWith("admin"));
    }

    @Test
    public void testAliasCurrentAccount() {
        AccountAllocator.allocateAccount("test2", "Admin");
        AccountAllocator.aliasCurrentAccount("primaryAdmin");

        UserAccount aliased = AccountAllocator.getAccount("primaryAdmin");
        Assert.assertNotNull(aliased);
        Assert.assertEquals(aliased.getUsername(), AccountAllocator.getCurrentAccount().getUsername());
    }

    @Test
    public void testMultipleRoleSwitch() {
        AccountAllocator.useAccount("adminAlias", "test3", "Admin");
        UserAccount admin = AccountAllocator.getCurrentAccount();
        Assert.assertEquals(admin.getRole(), "Admin");

        AccountAllocator.useAccount("standardAlias", "test3", "Standard");
        UserAccount standard = AccountAllocator.getCurrentAccount();
        Assert.assertEquals(standard.getRole(), "Standard");

        // Switch back to adminAlias
        AccountAllocator.useAccount("adminAlias");
        Assert.assertEquals(AccountAllocator.getCurrentAccount().getUsername(), admin.getUsername());
    }
}
