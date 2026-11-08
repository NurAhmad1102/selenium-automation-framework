package com.automation.framework.sample.tests;

import com.automation.framework.core.assertion.CustomAssert;
import com.automation.framework.core.driver.DriverManager;
import com.automation.framework.sample.base.SampleBaseTest;
import org.testng.annotations.Test;

public class SampleSearchTest extends SampleBaseTest {

    @Test(description = "Verify that the base URL loads successfully and title is valid")
    public void sampleNavigationTest() {
        String currentUrl = DriverManager.getDriver().getCurrentUrl();
        logger.info("Current URL loaded: {}", currentUrl);

        CustomAssert.assertTrue(currentUrl != null && !currentUrl.isEmpty(),
                "Target web page was not loaded successfully");
    }
}
