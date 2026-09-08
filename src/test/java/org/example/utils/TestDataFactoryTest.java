package org.example.utils;

import org.example.config.Config;
import org.example.config.slaSettingStructure.SingleConfig;
import org.example.utils.account.AccountAllocator;
import org.example.utils.actionUtils.JsonReader;
import org.example.utils.config.LoginDataRepository;
import org.example.utils.excelReader.ExcelReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestDataFactoryTest {

    @Test(description = "Verify JsonReader loads data.json from data/backoffice/slaStatus/data.json successfully")
    public void testJsonReaderLoadsSuccessfully() {
        Config config = JsonReader.getConfig();
        Assert.assertNotNull(config, "Config should not be null");
        Assert.assertNotNull(config.getUserlogin(), "Userlogin config should not be null");
        Assert.assertEquals(config.getUserlogin().getPassword(), "Arjuna13!");
    }

    @Test(description = "Verify TestDataFactory returns password and SLA config without ExceptionInInitializerError")
    public void testTestDataFactoryPasswordAndConfig() {
        String password = TestDataFactory.getPassword();
        Assert.assertNotNull(password, "Password should not be null");
        Assert.assertEquals(password, "Arjuna13!");

        SingleConfig singleConfig = TestDataFactory.getSingleConfig();
        Assert.assertNotNull(singleConfig, "SingleConfig should not be null");
        Assert.assertEquals(singleConfig.getSlaDay(), "15");
    }

    @Test(description = "Verify LoginDataRepository loads Excel data and AccountAllocator initializes without NoClassDefFoundError")
    public void testLoginDataRepositoryAndAccountAllocator() {
        ExcelReader.LoginData loginData = LoginDataRepository.getLoginData();
        Assert.assertNotNull(loginData, "Login data should not be null");
        Assert.assertFalse(loginData.getAccounts().isEmpty(), "Accounts list should not be empty");

        // Verify AccountAllocator allocation does not throw NoClassDefFoundError
        AccountAllocator.allocateAccount("testAllocationSmoke", "HR_OD");
        String allocatedNip = AccountAllocator.getNip();
        Assert.assertNotNull(allocatedNip, "Allocated NIP should not be null");
        Assert.assertFalse(allocatedNip.isEmpty(), "Allocated NIP should not be empty");
    }
}