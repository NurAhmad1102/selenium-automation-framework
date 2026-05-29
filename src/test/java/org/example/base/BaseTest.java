package org.example.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.example.drivers.DriverManager;
import org.example.utils.account.AccountAllocator;
import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.core.ThrowableOutcomeClassifier;
import org.example.utils.report.internal.model.OutcomeStatus;
import org.example.utils.report.internal.model.TestOutcome;
import org.example.utils.serviceUtils.NetworkMonitor;
import org.example.utils.serviceUtils.SeleniumUtils;
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
    private final SeleniumUtils seleniumUtils = new SeleniumUtils();

    protected abstract String getUrl();

    protected abstract String getRole();

    @BeforeMethod
    public void setUp(org.testng.ITestResult result) {
        result.setAttribute("reportManagedByBaseTest", true);
        String testName = result.getMethod().getMethodName();
        // Lifecycle is managed by Listeners, but we can add steps here
        ReportAPI.step("Setting up test environment for: " + testName);
        
        AccountAllocator.allocateAccount(testName, getRole());

        logger.info("Setting Up WebDriver for test: {}", testName);
        String browser = System.getProperty("browser", "chrome").trim().toLowerCase();
        WebDriver localDriver;

        switch (browser) {
            case "firefox":
                org.openqa.selenium.firefox.FirefoxOptions firefoxOptions = new org.openqa.selenium.firefox.FirefoxOptions();
                firefoxOptions.setAcceptInsecureCerts(true);
                firefoxOptions.setCapability("webSocketUrl", true);
                localDriver = new org.openqa.selenium.firefox.FirefoxDriver(firefoxOptions);
                break;
            case "edge":
                org.openqa.selenium.edge.EdgeOptions edgeOptions = new org.openqa.selenium.edge.EdgeOptions();
                edgeOptions.setAcceptInsecureCerts(true);
                edgeOptions.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
                edgeOptions.addArguments("--force-device-scale-factor=1");
                edgeOptions.setCapability("webSocketUrl", true);
                localDriver = new org.openqa.selenium.edge.EdgeDriver(edgeOptions);
                break;
            case "chrome":
            default:
                ChromeOptions options = new ChromeOptions();
                options.setAcceptInsecureCerts(true);
                options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
                options.addArguments("--force-device-scale-factor=1");
                options.setCapability("webSocketUrl", true);
                localDriver = new ChromeDriver(options);
                break;
        }

        DriverManager.setDriver(localDriver);

        DriverManager.getDriver().manage().window().maximize();
        DriverManager.getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300));

        try {
            if (localDriver instanceof org.openqa.selenium.devtools.HasDevTools) {
                NetworkMonitor monitor = new NetworkMonitor((org.openqa.selenium.devtools.HasDevTools) localDriver);
                monitor.startMonitoring();
                networkMonitor.set(monitor);
                NetworkMonitor.setCurrentMonitor(monitor);
                logger.info("Network monitoring started via CDP");
            } else {
                logger.warn("Driver does not support DevTools. Network monitoring skipped.");
            }
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
                try {
                    if (result.getStatus() == ITestResult.FAILURE || result.getThrowable() != null) {
                        // Flush only the relevant server error before the test failure.
                        networkMonitor.get().flushServerErrorsToReport(null, getTestEndInstant(result));
                    }
                } catch (Exception e) {
                    logger.error("Error flushing network errors", e);
                } finally {
                    networkMonitor.get().stopMonitoring();
                    networkMonitor.remove();
                    NetworkMonitor.removeCurrentMonitor();
                }
            }
            finalizeReport(result);
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

    private void finalizeReport(ITestResult result) {
        if (Boolean.TRUE.equals(result.getAttribute("reportFinalized"))) {
            return;
        }

        if (result.getStatus() == ITestResult.SKIP) {
            Throwable throwable = result.getThrowable();
            TestOutcome outcome = ThrowableOutcomeClassifier.classify(throwable);
            String reason = throwable != null ? throwable.getMessage() : "No reason provided";
            if (outcome.getStatus() == OutcomeStatus.BROKEN) {
                ReportAPI.broken("Test Setup Broken: " + reason, throwable);
                attachFailureScreenshot(result);
            } else if (outcome.getStatus() == OutcomeStatus.BLOCKED) {
                ReportAPI.blocked("Test Blocked: " + reason, throwable);
            } else if (outcome.getStatus() == OutcomeStatus.SKIP) {
                ReportAPI.skip("Test Skipped: " + reason, throwable);
            } else {
                ReportAPI.fail("Test Setup Failed: " + reason, throwable);
                attachFailureScreenshot(result);
            }
        } else if (result.getStatus() == ITestResult.FAILURE || result.getThrowable() != null) {
            TestOutcome outcome = ThrowableOutcomeClassifier.classify(result.getThrowable());
            if (outcome.getStatus() == OutcomeStatus.BROKEN) {
                ReportAPI.broken("Test Execution Broken", result.getThrowable());
            } else if (outcome.getStatus() == OutcomeStatus.BLOCKED) {
                ReportAPI.blocked("Test Blocked", result.getThrowable());
            } else {
                ReportAPI.fail("Test Execution Failed", result.getThrowable());
            }
            attachFailureScreenshot(result);
        } else {
            ReportAPI.pass("Test Execution Finished Successfully");
        }

        ReportAPI.flush();
        result.setAttribute("reportFinalized", true);
    }

    private void attachFailureScreenshot(ITestResult result) {
        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            logger.warn("Driver is null, screenshot cannot be captured for failed test: {}",
                    result.getMethod().getMethodName());
            return;
        }

        try {
            String screenshotPath = seleniumUtils.getScreenShotPath(result.getMethod().getMethodName(), driver);
            ReportAPI.addScreenshot(screenshotPath);
        } catch (Exception e) {
            logger.error("Failed to capture screenshot for failed test: {}",
                    result.getMethod().getMethodName(), e);
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
