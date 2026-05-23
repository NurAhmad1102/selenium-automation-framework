package org.example.utils;
import java.io.File;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportNg {

    private static volatile ExtentReports extent;

    public static ExtentReports getReportObject(){
        if (extent == null) {
            synchronized (ExtentReportNg.class) {
                if (extent == null) {
                    String path = System.getProperty("user.dir") + File.separator + "reports" + File.separator + "index.html";
                    ExtentSparkReporter reporter = new ExtentSparkReporter(path);
                    reporter.config().enableOfflineMode(true);
                    reporter.config().setReportName("Test Automation");
                    reporter.config().setDocumentTitle("Test Result");

                    extent = new ExtentReports();
                    extent.attachReporter(reporter);
                    extent.setSystemInfo("Tester", "Ahm Sign");
                }
            }
        }
        return extent;
    }

    public static String formatterTestName(String testName){
        String whiteSpaced = testName.replaceAll("([A-Z])", " $1");
        String[] words = whiteSpaced.split(" ");
        StringBuilder result = new StringBuilder();

        for (String word:words){
            result.append(Character.toUpperCase(word.charAt(0))).
                    append(word.substring(1).toLowerCase()).
                    append(" ");
        }

        return result.toString().trim();
    }

}
