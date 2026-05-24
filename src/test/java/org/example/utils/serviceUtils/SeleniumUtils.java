package org.example.utils.serviceUtils;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SeleniumUtils {

    public String getScreenShotPath(String testCastName, WebDriver driver) throws IOException {
        String RUN_TIME_STAMP = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        String folderName = "capture/" + RUN_TIME_STAMP;
        String FOLDER_PATH = System.getProperty("user.dir") + File.separator + "reports" + File.separator + folderName.replace("/", File.separator);

        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        File folder = new File(FOLDER_PATH);
        if(!folder.exists()) folder.mkdirs();

        String destinationFile = FOLDER_PATH + File.separator + testCastName + ".png";
        FileUtils.copyFile(source, new File(destinationFile));
        
        // Return relative path for ExtentReports embedding
        return folderName + "/" + testCastName + ".png";
    }
}
