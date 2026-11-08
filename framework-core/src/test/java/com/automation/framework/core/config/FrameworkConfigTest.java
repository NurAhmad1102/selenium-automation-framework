package com.automation.framework.core.config;

import org.testng.Assert;
import org.testng.annotations.Test;

public class FrameworkConfigTest {

    @Test
    public void testDefaultConfigurations() {
        Assert.assertNotNull(FrameworkConfig.getBrowser(), "Browser should have a default value");
        Assert.assertEquals(FrameworkConfig.getDefaultTimeout(), 15, "Default timeout should match framework.properties");
        Assert.assertEquals(FrameworkConfig.getPageLoadTimeout(), 300, "Page load timeout should match framework.properties");
        Assert.assertFalse(FrameworkConfig.isDevMode(), "Dev mode should be false by default");
    }

    @Test
    public void testSystemPropertyOverride() {
        String testKey = "custom.test.property";
        System.setProperty(testKey, "override-val");
        try {
            Assert.assertEquals(FrameworkConfig.get(testKey), "override-val");
        } finally {
            System.clearProperty(testKey);
        }
    }

    @Test
    public void testFallbackDefaultValue() {
        String nonExistentKey = "random.non.existent.key." + System.currentTimeMillis();
        Assert.assertEquals(FrameworkConfig.get(nonExistentKey, "fallback"), "fallback");
    }
}
