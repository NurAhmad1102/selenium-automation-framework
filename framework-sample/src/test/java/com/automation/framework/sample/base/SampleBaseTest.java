package com.automation.framework.sample.base;

import com.automation.framework.core.auth.LoginHandler;
import com.automation.framework.core.config.FrameworkConfig;
import com.automation.framework.core.test.BaseTest;
import com.automation.framework.sample.auth.SampleLoginHandler;

/**
 * Base test for sample application tests.
 */
public abstract class SampleBaseTest extends BaseTest {

    private final SampleLoginHandler loginHandler = new SampleLoginHandler();

    @Override
    protected String getUrl() {
        return FrameworkConfig.get("base.url", "https://example.com");
    }

    @Override
    protected LoginHandler getLoginHandler() {
        return loginHandler;
    }
}
