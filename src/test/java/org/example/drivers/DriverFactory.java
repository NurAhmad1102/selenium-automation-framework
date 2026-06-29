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

    private static WebDriver createChromeDriver(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
        options.addArguments("--force-device-scale-factor=1");
        options.setCapability("webSocketUrl", true);
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }
        return new ChromeDriver(options);
    }

    private static WebDriver createFirefoxDriver(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        options.setAcceptInsecureCerts(true);
        options.setCapability("webSocketUrl", true);
        if (headless) {
            options.addArguments("-headless");
        }
        return new FirefoxDriver(options);
    }

    private static WebDriver createEdgeDriver(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        options.setAcceptInsecureCerts(true);
        options.setExperimentalOption("excludeSwitches", new String[] { "enable-automation" });
        options.addArguments("--force-device-scale-factor=1");
        options.setCapability("webSocketUrl", true);
        if (headless) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--window-size=1920,1080");
        }
        return new EdgeDriver(options);
    }
}
