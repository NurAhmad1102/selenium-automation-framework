package org.example.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.example.drivers.DriverManager;
import org.example.utils.account.AccountAllocator;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;

public abstract class BaseTest {

    protected final Logger logger = LogManager.getLogger(getClass());

    protected abstract String getUrl();

    protected abstract String getRole();

    @BeforeMethod
    public void setUp(org.testng.ITestResult result) {
        String testName = result.getMethod().getMethodName();
        ThreadContext.put("testMethod", testName);
        AccountAllocator.allocateAccount(testName, getRole());

        logger.info("Setting Up WebDriver for test: {}", testName);
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
        options.setPageLoadStrategy(org.openqa.selenium.PageLoadStrategy.EAGER); // gak perlu nuggun image/asset Gd
//        options.addArguments("--headless=new", "--disable-gpu", "--window-size=1920,1080");

        WebDriver localDriver = new ChromeDriver(options);
        DriverManager.setDriver(localDriver);

        DriverManager.getDriver().manage().window().maximize();
        DriverManager.getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300)); // tamba waktu, serve lemot bet

        String targetUrl = getUrl();
        logger.info("Navigating to URL: {}", targetUrl);
        DriverManager.getDriver().get(targetUrl);
    }

    @Deprecated
    public WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        logger.info("Tear down started");
        try {
            if (DriverManager.getDriver() != null) {
                DriverManager.quitDriver();
                logger.info("Driver quit successfully");
            }
        } catch (Exception e) {
            logger.error("Error during tear down", e);
        } finally {
            AccountAllocator.releaseAccount();
            ThreadContext.clearAll();
        }
    }
}
