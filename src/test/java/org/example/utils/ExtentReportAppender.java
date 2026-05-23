package org.example.utils;

import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.example.listener.Listeners;

import java.io.Serializable;

/**
 * Custom Log4j2 Appender to bridge logs to ExtentReports.
 * Uses testMethod from ThreadContext to ensure logs go to the correct test.
 */
@Plugin(name = "ExtentReportAppender", category = "Core", printObject = true)
public class ExtentReportAppender extends AbstractAppender {

    protected ExtentReportAppender(String name, Filter filter, Layout<? extends Serializable> layout) {
        super(name, filter, layout, true, null);
    }

    @PluginFactory
    public static ExtentReportAppender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Filter") Filter filter,
            @PluginElement("Layout") Layout<? extends Serializable> layout) {
        return new ExtentReportAppender(name, filter, layout);
    }

    @Override
    public void append(LogEvent event) {
        // Validation: Verify if the log event belongs to the current active test
        String eventTestMethod = event.getContextData().getValue("testMethod");
        ExtentTest currentExtentTest = Listeners.getExtentTest();
        
        if (currentExtentTest == null) {
            return;
        }

        // Logic: Only log to Extent if we are sure the log belongs to THIS specific test execution
        // This prevents log bleeding between tests running in sequence on the same thread
        String activeTestMethod = org.apache.logging.log4j.ThreadContext.get("testMethod");
        
        if (eventTestMethod == null || !eventTestMethod.equals(activeTestMethod)) {
            // Log belongs to a previous test or is out of sync
            return;
        }

        Level level = event.getLevel();
        String message = event.getMessage().getFormattedMessage();

        if (level.isMoreSpecificThan(Level.ERROR)) {
            currentExtentTest.log(Status.FAIL, message);
        } else if (level.isMoreSpecificThan(Level.WARN)) {
            currentExtentTest.log(Status.WARNING, message);
        } else if (level.isMoreSpecificThan(Level.INFO)) {
            currentExtentTest.info(message);
        }
    }
}
