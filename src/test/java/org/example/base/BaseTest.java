package org.example.base;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.example.config.userLoginStructure.UserloginConfig;
import org.example.drivers.DriverManager;
import org.example.drivers.DriverFactory;
import org.example.mainPagesFactory.BackOfficeMainPagesFactory;
import org.example.mainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.utils.account.AccountAllocator;
import org.example.utils.account.UserRole;
import org.example.utils.report.core.ReportAPI;
import org.example.utils.report.internal.storage.ThreadLocalStore;
import org.example.utils.report.core.ThrowableOutcomeClassifier;
import org.example.utils.report.internal.model.OutcomeStatus;
import org.example.utils.report.internal.model.TestOutcome;
import org.example.utils.serviceUtils.NetworkMonitor;
import org.example.utils.serviceUtils.SeleniumUtils;
import org.example.utils.DevModeUtils;
import org.openqa.selenium.JavascriptExecutor;
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
    private final ThreadLocal<String> currentTestName = new ThreadLocal<>();
    private final ThreadLocal<OfficeSessionSwitcher> officeSession = new ThreadLocal<>();
    private final SeleniumUtils seleniumUtils = new SeleniumUtils();

    protected abstract String getUrl();

    protected abstract String getRole();

    @BeforeMethod
    public void setUp(org.testng.ITestResult result) {
        result.setAttribute("reportManagedByBaseTest", true);
        String testName = result.getMethod().getMethodName();
        currentTestName.set(testName);
        // Lifecycle is managed by Listeners, but we can add steps here
        ReportAPI.step("Setting up test environment for: " + testName);
        
        AccountAllocator.allocateAccount(testName, getRole());

        logger.info("Setting Up WebDriver for test: {}", testName);
        WebDriver localDriver = DriverFactory.createDriver();

        DriverManager.setDriver(localDriver);

        if (!DevModeUtils.isDevMode()) {
            DriverManager.getDriver().manage().window().maximize();
            DriverManager.getDriver().manage().timeouts().pageLoadTimeout(Duration.ofSeconds(300));
        }

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
        if (DevModeUtils.isDevMode()) {
            logger.info("[DEV-MODE] Active. Skipping initial navigation to URL: {}", targetUrl);
        } else {
            logger.info("Navigating to URL: {}", targetUrl);
            DriverManager.getDriver().get(targetUrl);
        }
    }

    protected String currentTestName() {
        String testName = currentTestName.get();
        if (testName == null || testName.isEmpty()) {
            throw new IllegalStateException("Current test name is not initialized. Ensure setUp() runs first.");
        }
        return testName;
    }

    protected void useCurrentUserAs(String alias) {
        AccountAllocator.aliasCurrentAccount(alias);
    }

    protected OfficeSessionSwitcher officeSession() {
        OfficeSessionSwitcher session = officeSession.get();
        if (session == null) {
            session = new OfficeSessionSwitcher(this);
            officeSession.set(session);
        }
        return session;
    }

    protected BackOfficeMainPagesFactory backOfficePages() {
        return officeSession().backOfficePages();
    }

    protected FrontOfficeMainPagesFactory frontOfficePages() {
        return officeSession().frontOfficePages();
    }

    protected void useBackOfficeUser(String alias, UserRole role) {
        officeSession().useBackOfficeUser(alias, role);
    }

    protected void useBackOfficeUser(String alias, String role) {
        officeSession().useBackOfficeUser(alias, role);
    }

    protected void useBackOfficeUser(String alias) {
        officeSession().useBackOfficeUser(alias);
    }

    protected void useFrontOfficeUser(String alias, UserRole role) {
        officeSession().useFrontOfficeUser(alias, role);
    }

    protected void useFrontOfficeUser(String alias, String role) {
        officeSession().useFrontOfficeUser(alias, role);
    }

    protected void useFrontOfficeUser(String alias) {
        officeSession().useFrontOfficeUser(alias);
    }

    protected UserloginConfig currentAccount() {
        UserloginConfig account = AccountAllocator.getCurrentAccount();
        if (account == null) {
            throw new IllegalStateException("No current account is allocated for this test thread.");
        }
        return account;
    }

    protected UserloginConfig account(String alias) {
        UserloginConfig account = AccountAllocator.getAccount(alias);
        if (account == null) {
            throw new IllegalArgumentException("Account alias not found for this test thread: " + alias);
        }
        return account;
    }

    protected String currentAccountAlias() {
        return AccountAllocator.getCurrentAlias();
    }

    protected String currentNip() {
        return currentAccount().getNip();
    }

    protected String currentUsername() {
        return currentAccount().getUsername();
    }

    protected String accountNip(String alias) {
        return account(alias).getNip();
    }

    protected String accountUsername(String alias) {
        return account(alias).getUsername();
    }

    protected void resetBrowserSession() {
        if (DevModeUtils.isDevMode()) {
            logger.info("[DEV-MODE] Skipping resetBrowserSession to preserve developer login session.");
            return;
        }

        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            throw new IllegalStateException("WebDriver is not initialized. Cannot reset browser session.");
        }

        try {
            driver.manage().deleteAllCookies();
        } catch (Exception e) {
            logger.warn("Failed to delete browser cookies during account switch.", e);
        }

        if (driver instanceof JavascriptExecutor) {
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "window.localStorage.clear(); window.sessionStorage.clear();");
            } catch (Exception e) {
                logger.warn("Failed to clear browser storage during account switch.", e);
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        logger.info("Tear down started");
        try {
            // Phase 1: Network Monitor Teardown
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
            } catch (Exception e) {
                logger.error("Error in network monitor teardown phase", e);
            }

            // Phase 2: Report Finalization
            try {
                finalizeReport(result);
            } catch (Exception e) {
                logger.error("Error finalizing report", e);
            }

            // Phase 3: WebDriver Quit
            try {
                if (DriverManager.getDriver() != null) {
                    DriverManager.quitDriver();
                    logger.info("Driver quit successfully");
                }
            } catch (Exception e) {
                logger.error("Error quitting driver", e);
            }
        } catch (Exception e) {
            logger.error("Unexpected error during tear down pipeline", e);
        } finally {
            // Phase 4: ThreadLocal and allocator cleanup (essential to prevent leaks across runs)
            try {
                networkMonitor.remove();
                NetworkMonitor.removeCurrentMonitor();
            } catch (Exception e) {
                logger.error("Error removing network monitor thread locals", e);
            } finally {
                try {
                    OfficeSessionSwitcher session = officeSession.get();
                    if (session != null) {
                        session.clear();
                        officeSession.remove();
                    }
                } catch (Exception e) {
                    logger.error("Error clearing office session", e);
                } finally {
                    try {
                        AccountAllocator.releaseAllAccounts();
                    } catch (Exception e) {
                        logger.error("Error releasing accounts", e);
                    } finally {
                        try {
                            currentTestName.remove();
                        } catch (Exception e) {
                            logger.error("Error removing currentTestName", e);
                        } finally {
                            try {
                                ThreadLocalStore.clear();
                            } catch (Exception e) {
                                logger.error("Error clearing ThreadLocalStore", e);
                            } finally {
                                ThreadContext.clearAll();
                            }
                        }
                    }
                }
            }
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
