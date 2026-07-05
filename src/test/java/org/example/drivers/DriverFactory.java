package org.example.drivers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class DriverFactory {
    private static final Logger logger = LogManager.getLogger(DriverFactory.class);

    public static WebDriver createDriver() {
        String browser = System.getProperty("browser", "chrome").trim().toLowerCase();
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "false").trim());

        logger.info("Initializing WebDriver: {} (Headless: {})", browser, headless);

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
            root = java.nio.file.Paths.get(System.getProperty("user.dir"), ".selenium").toString();
        }
        String buildTag = System.getenv("BUILD_TAG");
        if (buildTag == null || buildTag.isBlank()) {
            buildTag = "local";
        }
        buildTag = buildTag.replaceAll("[^a-zA-Z0-9._-]", "_");
        
        String sanitizedBrowser = browserName.replaceAll("[^a-zA-Z0-9._-]", "_").toLowerCase();
        
        String profilePath = java.nio.file.Paths.get(
            root,
            buildTag,
            sanitizedBrowser + "-profile-" + Thread.currentThread().getId() + "-" + java.util.UUID.randomUUID()
        ).toString();
        
        try {
            java.nio.file.Files.createDirectories(java.nio.file.Paths.get(profilePath));
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create browser profile directory: " + profilePath, e);
        }
        return profilePath;
    }

    private static WebDriver createChromeDriver(boolean headless) {
        String profile = createProfilePath("chrome");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--user-data-dir=" + profile);
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
        options.addArguments("--force-device-scale-factor=1");
        options.setCapability("webSocketUrl", true);
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }
        org.openqa.selenium.chrome.ChromeDriverService service = new org.openqa.selenium.chrome.ChromeDriverService.Builder()
            .withLogFile(java.nio.file.Paths.get(profile, "chromedriver.log").toFile())
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
        }
        org.openqa.selenium.firefox.GeckoDriverService service = new org.openqa.selenium.firefox.GeckoDriverService.Builder()
            .withLogFile(java.nio.file.Paths.get(profile, "geckodriver.log").toFile())
            .build();
        return new FirefoxDriver(service, options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        String profile = createProfilePath("edge");
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--user-data-dir=" + profile);
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
        options.addArguments("--force-device-scale-factor=1");
        options.setCapability("webSocketUrl", true);
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }
        org.openqa.selenium.edge.EdgeDriverService service = new org.openqa.selenium.edge.EdgeDriverService.Builder()
            .withLogFile(java.nio.file.Paths.get(profile, "edgedriver.log").toFile())
            .build();
        return new EdgeDriver(service, options);
    }
}
