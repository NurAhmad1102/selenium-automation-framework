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
        String FOLDER_PATH = System.getProperty("user.dir")+ "//reports//capture//"+ RUN_TIME_STAMP;

//        TakesScreenshot screenshot = (TakesScreenshot) driver;
//        File source = screenshot.getScreenshotAs(OutputType.FILE);
        File source = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);

        File folder = new File(FOLDER_PATH);
        if(!folder.exists()) folder.mkdirs(); //buat kalo gak ada cok

        String destinationFile = FOLDER_PATH+"//"+testCastName+".png";
        FileUtils.copyFile(source, new File(destinationFile));
        return destinationFile;
    }

}
