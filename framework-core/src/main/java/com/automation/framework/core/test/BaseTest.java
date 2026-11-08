package com.automation.framework.core.test;

import com.automation.framework.core.account.AccountAllocator;
import com.automation.framework.core.account.UserAccount;
import com.automation.framework.core.auth.LoginHandler;
import com.automation.framework.core.auth.NoOpLoginHandler;
import com.automation.framework.core.config.FrameworkConfig;
import com.automation.framework.core.driver.DevModeUtils;
import com.automation.framework.core.driver.DriverFactory;
import com.automation.framework.core.driver.DriverManager;
import com.automation.framework.core.network.NetworkMonitor;
import com.automation.framework.core.report.core.ReportAPI;
import com.automation.framework.core.report.core.ThrowableOutcomeClassifier;
import com.automation.framework.core.report.internal.model.OutcomeStatus;
import com.automation.framework.core.report.internal.model.TestOutcome;
import com.automation.framework.core.report.internal.storage.ThreadLocalStore;
import com.automation.framework.core.utils.SeleniumUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.ThreadContext;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.HasDevTools;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.time.Duration;
import java.time.Instant;

/**
 * Agnostic abstract test base class providing automated WebDriver lifecycle management,
 * pluggable authentication, DevTools network error capturing, and ExtentReports integration.
 */
public abstract class BaseTest {

    protected final Logger logger = LogManager.getLogger(getClass());
    private final ThreadLocal<NetworkMonitor> networkMonitor = new ThreadLocal<>();
    private final ThreadLocal<String> currentTestName = new ThreadLocal<>();
    private final SeleniumUtils seleniumUtils = new SeleniumUtils();

    /**
     * Target URL for the test execution. Defaults to FrameworkConfig 'base.url' if not overridden.
     */
    protected String getUrl() {
        return FrameworkConfig.getBaseUrl();
    }

    /**
     * Optional role to allocate from the AccountAllocator pool. Return null if no account needed.
     */
    protected String getRole() {
        return null;
    }

    /**
     * Pluggable LoginHandler for authenticating into the target application.
     */
    protected LoginHandler getLoginHandler() {
        return new NoOpLoginHandler();
    }

    @BeforeMethod
    public void setUp(ITestResult result) {
        result.setAttribute("reportManagedByBaseTest", true);
        String testName = result.getMethod().getMethodName();
        currentTestName.set(testName);
        ReportAPI.step("Setting up test execution context: " + testName);

        String role = getRole();
        if (role != null && !role.isBlank()) {
            AccountAllocator.allocateAccount(testName, role);
        }

        logger.info("Initializing WebDriver for test: {}", testName);
        WebDriver localDriver = DriverFactory.createDriver();
        DriverManager.setDriver(localDriver);

        if (!DevModeUtils.isDevMode()) {
            if (FrameworkConfig.isHeadless()) {
                DriverManager.getDriver().manage().window().setSize(new Dimension(1920, 1080));
            } else {
                DriverManager.getDriver().manage().window().maximize();
            }
            DriverManager.getDriver().manage().timeouts().pageLoadTimeout(
                    Duration.ofSeconds(FrameworkConfig.getPageLoadTimeout()));
        }

        try {
            if (localDriver instanceof HasDevTools) {
                NetworkMonitor monitor = new NetworkMonitor((HasDevTools) localDriver);
                monitor.startMonitoring();
                networkMonitor.set(monitor);
                NetworkMonitor.setCurrentMonitor(monitor);
                logger.info("Network monitoring started via Chrome DevTools Protocol.");
            } else {
                logger.warn("Driver does not implement HasDevTools. CDP Network monitoring skipped.");
            }
        } catch (Exception e) {
            logger.error("Failed to start CDP network monitoring", e);
        }

        String targetUrl = getUrl();
        if (targetUrl != null && !targetUrl.isBlank()) {
            if (DevModeUtils.isDevMode()) {
                logger.info("[DEV-MODE] Active. Skipping initial navigation to URL: {}", targetUrl);
            } else {
                logger.info("Navigating to target URL: {}", targetUrl);
                DriverManager.getDriver().get(targetUrl);
            }
        }

        // Execute login if an account was allocated
        if (role != null && !role.isBlank()) {
            UserAccount account = currentAccount();
            LoginHandler handler = getLoginHandler();
            if (handler != null && (!DevModeUtils.isDevMode() || !handler.isLoggedIn(localDriver))) {
                ReportAPI.step("Authenticating user [" + account.getUsername() + "] with role [" + account.getRole() + "]");
                handler.login(localDriver, account);
            }
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

    protected <T extends UserAccount> T currentAccount() {
        T account = AccountAllocator.getCurrentAccount();
        if (account == null) {
            throw new IllegalStateException("No current account is allocated for this test thread.");
        }
        return account;
    }

    protected <T extends UserAccount> T account(String alias) {
        T account = AccountAllocator.getAccount(alias);
        if (account == null) {
            throw new IllegalArgumentException("Account alias not found for this test thread: " + alias);
        }
        return account;
    }

    protected String currentAccountAlias() {
        return AccountAllocator.getCurrentAlias();
    }

    protected void resetBrowserSession() {
        if (DevModeUtils.isDevMode()) {
            logger.info("[DEV-MODE] Skipping resetBrowserSession to preserve active developer session.");
            return;
        }

        WebDriver driver = DriverManager.getDriver();
        if (driver == null) {
            return;
        }

        try {
            driver.manage().deleteAllCookies();
        } catch (Exception e) {
            logger.warn("Failed to delete browser cookies during session reset.", e);
        }

        if (driver instanceof JavascriptExecutor) {
            try {
                ((JavascriptExecutor) driver).executeScript(
                        "window.localStorage.clear(); window.sessionStorage.clear();");
            } catch (Exception e) {
                logger.warn("Failed to clear browser storage during session reset.", e);
            }
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        logger.info("Tear down started for test: {}", result.getMethod().getMethodName());
        try {
            // Phase 1: Network Monitor Teardown
            try {
                if (networkMonitor.get() != null) {
                    try {
                        if (result.getStatus() == ITestResult.FAILURE || result.getThrowable() != null) {
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
            // Phase 4: ThreadLocal and allocator cleanup
            try {
                networkMonitor.remove();
                NetworkMonitor.removeCurrentMonitor();
            } catch (Exception e) {
                logger.error("Error removing network monitor thread locals", e);
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

    private void finalizeReport(ITestResult result) {
        if (Boolean.TRUE.equals(result.getAttribute("reportFinalized"))) {
            return;
        }

        if (result.getStatus() == ITestResult.SKIP) {
            Throwable throwable = result.getThrowable();
            TestOutcome outcome = ThrowableOutcomeClassifier.classify(throwable);
            String reason = throwable != null && throwable.getMessage() != null && !throwable.getMessage().isBlank()
                    ? throwable.getMessage()
                    : "No reason provided";
            String setupPrefix = "PRECONDITION FAILED [@BeforeMethod]: " + reason;
            if (outcome.getStatus() == OutcomeStatus.BROKEN) {
                ReportAPI.broken(setupPrefix, throwable);
                attachFailureScreenshot(result);
            } else if (outcome.getStatus() == OutcomeStatus.BLOCKED) {
                ReportAPI.blocked(setupPrefix, throwable);
            } else if (outcome.getStatus() == OutcomeStatus.SKIP) {
                ReportAPI.skip(setupPrefix, throwable);
            } else {
                ReportAPI.fail(setupPrefix, throwable);
                attachFailureScreenshot(result);
            }
        } else if (result.getStatus() == ITestResult.FAILURE || result.getThrowable() != null) {
            Throwable throwable = result.getThrowable();
            TestOutcome outcome = ThrowableOutcomeClassifier.classify(throwable);
            String reason = throwable != null && throwable.getMessage() != null && !throwable.getMessage().isBlank()
                    ? throwable.getMessage()
                    : "No exception message provided";
            String failMsg = "Test Execution Failed: " + reason;
            if (outcome.getStatus() == OutcomeStatus.BROKEN) {
                ReportAPI.broken(failMsg, throwable);
            } else if (outcome.getStatus() == OutcomeStatus.BLOCKED) {
                ReportAPI.blocked(failMsg, throwable);
            } else {
                ReportAPI.fail(failMsg, throwable);
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
