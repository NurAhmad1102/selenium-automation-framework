package org.example.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.example.drivers.DriverManager;
import org.example.listener.Listeners;
import org.example.utils.account.AccountAllocator;
import org.example.utils.serviceUtils.NetworkMonitor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.time.Instant;

public abstract class BaseTest {

    protected final Logger logger = LogManager.getLogger(getClass());
    private ThreadLocal<NetworkMonitor> networkMonitor = new ThreadLocal<>();

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

        ChromeDriver localDriver = new ChromeDriver(options);
        DriverManager.setDriver(localDriver);

        DriverManager.getDriver().manage().window().maximize();
        DriverManager.getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300));

        try {
            NetworkMonitor monitor = new NetworkMonitor(localDriver);
            monitor.startMonitoring();
            networkMonitor.set(monitor);
            NetworkMonitor.setCurrentMonitor(monitor);
            logger.info("Network monitoring started via CDP");
        } catch (Exception e) {
            logger.error("Failed to start network monitoring", e);
        }

        String targetUrl = getUrl();
        logger.info("Navigating to URL: {}", targetUrl);
        DriverManager.getDriver().get(targetUrl);
    }

    @Deprecated
    public WebDriver getDriver() {
        return DriverManager.getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        logger.info("Tear down started");
        try {
            if (networkMonitor.get() != null) {
                if (result.getStatus() == ITestResult.FAILURE || result.getThrowable() != null) {
                    networkMonitor.get().flushServerErrorsToReport(
                            Listeners.getExtentTest(),
                            getTestEndInstant(result));
                }
                networkMonitor.get().stopMonitoring();
                networkMonitor.remove();
                NetworkMonitor.removeCurrentMonitor();
            }
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

    private Instant getTestEndInstant(ITestResult result) {
        long endMillis = result.getEndMillis();
        if (endMillis <= 0) {
            endMillis = System.currentTimeMillis();
        }
        return Instant.ofEpochMilli(endMillis);
    }
}
