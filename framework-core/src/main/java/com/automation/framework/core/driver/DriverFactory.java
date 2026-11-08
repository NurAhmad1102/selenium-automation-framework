package com.automation.framework.core.driver;

import com.automation.framework.core.config.FrameworkConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeDriverService;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.GeckoDriverService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Agnostic WebDriver factory with support for Chrome, Firefox, Edge,
 * isolated profile directories per execution thread, and developer mode attaching.
 */
public final class DriverFactory {

    private static final Logger logger = LogManager.getLogger(DriverFactory.class);

    private DriverFactory() {
    }

    public static WebDriver createDriver() {
        String browser = FrameworkConfig.getBrowser();
        boolean headless = FrameworkConfig.isHeadless();

        if (DevModeUtils.isDevMode()) {
            logger.info("[DEV-MODE] Active. Attaching to existing browser session.");
        } else {
            logger.info("Initializing WebDriver: {} (Headless: {})", browser, headless);
        }

        switch (browser) {
            case "firefox":
                return createFirefoxDriver(headless);
            case "edge":
                return createEdgeDriver(headless);
            case "chrome":
            default:
                return createChromeDriver(headless);
        }
    }

    private static String createProfilePath(String browserName) {
        String root = System.getenv("SELENIUM_PROFILE_ROOT");
        if (root == null || root.isBlank()) {
            root = Paths.get(System.getProperty("user.dir"), ".selenium").toString();
        }
        String buildTag = System.getenv("BUILD_TAG");
        if (buildTag == null || buildTag.isBlank()) {
            buildTag = "local";
        }
        buildTag = buildTag.replaceAll("[^a-zA-Z0-9._-]", "_");

        String sanitizedBrowser = browserName.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();

        String profilePath = Paths.get(
                root,
                buildTag,
                sanitizedBrowser + "-profile-" + Thread.currentThread().getId() + "-" + UUID.randomUUID()
        ).toString();

        try {
            Files.createDirectories(Paths.get(profilePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to create browser profile directory: " + profilePath, e);
        }
        return profilePath;
    }

    private static void switchToActiveDevTab(WebDriver driver) {
        try {
            Set<String> handles = driver.getWindowHandles();
            if (handles.size() > 1) {
                for (String handle : handles) {
                    driver.switchTo().window(handle);
                    String url = driver.getCurrentUrl();
                    if (url != null && !url.equals("about:blank") && !url.startsWith("chrome://") && !url.startsWith("edge://")) {
                        logger.info("[DEV-MODE] Switched focus to active application tab: {}", url);
                        return;
                    }
                }
            } else if (handles.size() == 1) {
                driver.switchTo().window(handles.iterator().next());
                logger.info("[DEV-MODE] Attached to single tab (URL: {})", driver.getCurrentUrl());
            }
        } catch (Exception e) {
            logger.warn("[DEV-MODE] Could not switch to active tab: {}", e.getMessage());
        }
    }

    private static WebDriver createChromeDriver(boolean headless) {
        if (DevModeUtils.isDevMode()) {
            String devPort = DevModeUtils.getDevPort();
            logger.info("[DEV-MODE] Attaching ChromeDriver to running browser on 127.0.0.1:{}", devPort);
            ChromeOptions options = new ChromeOptions();
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:" + devPort);
            options.setAcceptInsecureCerts(true);
            try {
                WebDriver driver = new ChromeDriver(options);
                switchToActiveDevTab(driver);
                return driver;
            } catch (Exception e) {
                logger.error("[DEV-MODE] Failed to attach to Chrome on port {}. Ensure Chrome is running with '--remote-debugging-port={}'", devPort, devPort, e);
                throw e;
            }
        }

        String profile = createProfilePath("chrome");
        ChromeOptions options = new ChromeOptions();

        Map<String, Object> prefs = new HashMap<>();
        prefs.put("credentials_enable_service", false);
        prefs.put("profile.password_manager_enabled", false);
        prefs.put("profile.password_manager_leak_detection", false);
        prefs.put("autofill.profile_enabled", false);
        prefs.put("autofill.credit_card_enabled", false);
        options.setExperimentalOption("prefs", prefs);

        options.addArguments("--user-data-dir=" + profile);
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--force-device-scale-factor=1");
        options.setCapability("webSocketUrl", true);

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }

        ChromeDriverService service = new ChromeDriverService.Builder()
                .withLogFile(Paths.get(profile, "chromedriver.log").toFile())
                .build();

        return new ChromeDriver(service, options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        String profile = createProfilePath("firefox");
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("-profile");
        options.addArguments(profile);
        options.setAcceptInsecureCerts(true);
        options.setCapability("webSocketUrl", true);

        if (headless) {
            options.addArguments("-headless");
            options.addArguments("--width=1920");
            options.addArguments("--height=1080");
        }

        GeckoDriverService service = new GeckoDriverService.Builder()
                .withLogFile(Paths.get(profile, "geckodriver.log").toFile())
                .build();

        return new FirefoxDriver(service, options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        if (DevModeUtils.isDevMode()) {
            String devPort = DevModeUtils.getDevPort();
            logger.info("[DEV-MODE] Attaching EdgeDriver to running browser on 127.0.0.1:{}", devPort);
            EdgeOptions options = new EdgeOptions();
            options.setExperimentalOption("debuggerAddress", "127.0.0.1:" + devPort);
            options.setAcceptInsecureCerts(true);
            try {
                WebDriver driver = new EdgeDriver(options);
                switchToActiveDevTab(driver);
                return driver;
            } catch (Exception e) {
                logger.error("[DEV-MODE] Failed to attach to Edge on port {}. Ensure Edge is running with '--remote-debugging-port={}'", devPort, devPort, e);
                throw e;
            }
        }

        String profile = createProfilePath("edge");
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--user-data-dir=" + profile);
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.addArguments("--force-device-scale-factor=1");
        options.setCapability("webSocketUrl", true);

        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }

        EdgeDriverService service = new EdgeDriverService.Builder()
                .withLogFile(Paths.get(profile, "edgedriver.log").toFile())
                .build();

        return new EdgeDriver(service, options);
    }
}
