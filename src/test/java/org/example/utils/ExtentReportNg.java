package org.example.utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;

public class ExtentReportNg {

    static ExtentReports extent;

    public static ExtentReports getReportObject(){
        String path = System.getProperty("user.dir")+"\\reports\\index.html";
        ExtentSparkReporter reporter = new ExtentSparkReporter(path);
        reporter.config().enableOfflineMode(true);
        reporter.config().setReportName("Test Automation");
        reporter.config().setDocumentTitle("Test Result");

        extent = new ExtentReports();
        extent.attachReporter(reporter);
        extent.setSystemInfo("Tester", "Ahm Sign");
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
