package com.automation.framework.core.utils;

import com.automation.framework.core.config.FrameworkConfig;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Common Selenium utility methods including timestamped screenshot captures.
 */
public class SeleniumUtils {

    public String getScreenShotPath(String testCaseName, WebDriver driver) throws IOException {
        String runTimeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String folderName = "capture/" + runTimeStamp;
        String reportDir = FrameworkConfig.getReportDirectory();
        String folderPath = System.getProperty("user.dir") + File.separator + reportDir + File.separator + folderName.replace("/", File.separator);

        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        String fileName = testCaseName + "_" + System.currentTimeMillis() + ".png";
        String destinationFile = folderPath + File.separator + fileName;
        FileUtils.copyFile(source, new File(destinationFile));

        // Return relative path for report embedding
        return folderName + "/" + fileName;
    }
}
