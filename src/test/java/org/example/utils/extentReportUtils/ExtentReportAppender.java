package org.example.utils.extentReportUtils;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.example.utils.report.core.ReportAPI;

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
        String eventTestMethod = event.getContextData().getValue("testMethod");
        String activeTestMethod = ThreadContext.get("testMethod");

        if (!ReportAPI.isTestActive()) {
            return;
        }

        if (eventTestMethod != null && activeTestMethod != null && !eventTestMethod.equals(activeTestMethod)) {
            return;
        }

        Level level = event.getLevel();
        String message = event.getMessage().getFormattedMessage();

        if (level.isMoreSpecificThan(Level.ERROR)) {
            ReportAPI.fail(message, event.getThrown());
        } else if (level.isMoreSpecificThan(Level.WARN)) {
            ReportAPI.warning("WARN: " + message);
        } else if (level.isMoreSpecificThan(Level.INFO)) {
            ReportAPI.step(message);
        }
    }
}
