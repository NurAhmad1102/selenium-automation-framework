package com.automation.framework.core.report.implementations.extent;

import com.automation.framework.core.config.FrameworkConfig;
import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

import java.io.File;

public final class ExtentReportNg {

    private static volatile ExtentReports extent;

    private ExtentReportNg() {
    }

    public static ExtentReports getReportObject() {
        if (extent == null) {
            synchronized (ExtentReportNg.class) {
                if (extent == null) {
                    String reportDir = FrameworkConfig.getReportDirectory();
                    String path = System.getProperty("user.dir") + File.separator + reportDir + File.separator + "index.html";
                    ExtentSparkReporter reporter = new ExtentSparkReporter(path);
                    reporter.config().enableOfflineMode(true);
                    reporter.config().setReportName(FrameworkConfig.get("report.title", "Test Automation Execution Report"));
                    reporter.config().setDocumentTitle(FrameworkConfig.get("report.doc.title", "Automation Test Results"));

                    // Inject Custom UI
                    reporter.config().setCss(ExtentUIInjector.getCustomCss());
                    reporter.config().setJs(ExtentUIInjector.getCustomJs());

                    extent = new ExtentReports();
                    extent.attachReporter(reporter);
                    extent.setSystemInfo("Framework", "Enterprise Selenium Core");
                    extent.setSystemInfo("Tester", FrameworkConfig.get("report.tester", System.getProperty("user.name")));
                }
            }
        }
        return extent;
    }

    public static String formatterTestName(String testName) {
        if (testName == null || testName.isBlank()) {
            return "Unnamed Test";
        }
        String whiteSpaced = testName.replaceAll("([A-Z])", " $1");
        String[] words = whiteSpaced.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            result.append(Character.toUpperCase(word.charAt(0)))
                    .append(word.substring(1).toLowerCase())
                    .append(" ");
        }

        return result.toString().trim();
    }
}
