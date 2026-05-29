package org.example.utils.serviceUtils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.devtools.Command;
import org.openqa.selenium.devtools.DevTools;
import org.openqa.selenium.devtools.Event;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

public class CdpVersionHelper {
    private static final Logger logger = LogManager.getLogger(CdpVersionHelper.class);
    
    private final String packageName;
    private final Class<?> networkClass;
    private final Class<?> requestIdClass;
    private final Constructor<?> requestIdConstructor;

    public CdpVersionHelper(WebDriver driver) {
        String majorVersion = getMajorBrowserVersion(driver);
        String pkg = "org.openqa.selenium.devtools.v" + majorVersion;
        Class<?> netCls = null;
        
        logger.info("Detecting DevTools version matching browser major version: v{}", majorVersion);
        try {
            netCls = Class.forName(pkg + ".network.Network");
        } catch (ClassNotFoundException e) {
            logger.warn("DevTools package [{}] not found on classpath. Falling back to v148...", pkg);
            pkg = "org.openqa.selenium.devtools.v148";
            try {
                netCls = Class.forName(pkg + ".network.Network");
            } catch (ClassNotFoundException ex) {
                logger.warn("DevTools package v148 not found. Searching for available fallback versions...");
                String[] fallbacks = {"130", "129", "128", "127", "126", "125"};
                for (String v : fallbacks) {
                    try {
                        pkg = "org.openqa.selenium.devtools.v" + v;
                        netCls = Class.forName(pkg + ".network.Network");
                        logger.info("Found fallback DevTools package: {}", pkg);
                        break;
                    } catch (ClassNotFoundException ignored) {}
                }
            }
        }

        if (netCls == null) {
            throw new RuntimeException("No compatible DevTools package found on the classpath!");
        }

        this.packageName = pkg;
        this.networkClass = netCls;
        
        try {
            this.requestIdClass = Class.forName(pkg + ".network.model.RequestId");
            this.requestIdConstructor = this.requestIdClass.getConstructor(String.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize DevTools RequestId reflection templates", e);
        }
        
        logger.info("DevTools version resolved successfully using package: {}", this.packageName);
    }

    private String getMajorBrowserVersion(WebDriver driver) {
        if (driver instanceof HasCapabilities) {
            String version = ((HasCapabilities) driver).getCapabilities().getBrowserVersion();
            if (version != null && version.contains(".")) {
                return version.split("\\.")[0];
            }
        }
        return "148"; // Default fallback major version
    }

    public Object createRequestId(String id) {
        try {
            return requestIdConstructor.newInstance(id);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate RequestId with value: " + id, e);
        }
    }

    // Network.enable
    @SuppressWarnings("unchecked")
    public Command<Void> enableNetwork() {
        try {
            Method enableMethod = null;
            for (Method m : networkClass.getMethods()) {
                if (m.getName().equals("enable")) {
                    enableMethod = m;
                    break;
                }
            }
            if (enableMethod == null) {
                throw new NoSuchMethodException("Method enable not found in Network class");
            }

            Class<?>[] parameterTypes = enableMethod.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] == Optional.class) {
                    args[i] = Optional.empty();
                } else {
                    args[i] = null;
                }
            }

            return (Command<Void>) enableMethod.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke Network.enable via reflection", e);
        }
    }

    // Network.disable
    @SuppressWarnings("unchecked")
    public Command<Void> disableNetwork() {
        try {
            Method method = networkClass.getMethod("disable");
            return (Command<Void>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke Network.disable via reflection", e);
        }
    }

    // Network.getResponseBody
    @SuppressWarnings("unchecked")
    public Command<?> getResponseBody(Object requestIdObj) {
        try {
            Method method = networkClass.getMethod("getResponseBody", requestIdClass);
            return (Command<?>) method.invoke(null, requestIdObj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke Network.getResponseBody via reflection", e);
        }
    }

    // Network.requestWillBeSent() Event
    @SuppressWarnings("unchecked")
    public Event<?> requestWillBeSentEvent() {
        try {
            Method method = networkClass.getMethod("requestWillBeSent");
            return (Event<?>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Network.requestWillBeSent Event", e);
        }
    }

    // Network.responseReceived() Event
    @SuppressWarnings("unchecked")
    public Event<?> responseReceivedEvent() {
        try {
            Method method = networkClass.getMethod("responseReceived");
            return (Event<?>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Network.responseReceived Event", e);
        }
    }

    // Network.loadingFailed() Event
    @SuppressWarnings("unchecked")
    public Event<?> loadingFailedEvent() {
        try {
            Method method = networkClass.getMethod("loadingFailed");
            return (Event<?>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Network.loadingFailed Event", e);
        }
    }

    // Network.loadingFinished() Event
    @SuppressWarnings("unchecked")
    public Event<?> loadingFinishedEvent() {
        try {
            Method method = networkClass.getMethod("loadingFinished");
            return (Event<?>) method.invoke(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Network.loadingFinished Event", e);
        }
    }

    // Property Reflection Accessors
    
    public static Object invokeGetter(Object obj, String methodName) {
        if (obj == null) return null;
        try {
            Method method = obj.getClass().getMethod(methodName);
            return method.invoke(obj);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke reflection getter: " + methodName + " on " + obj.getClass().getName(), e);
        }
    }

    public static Object resolveOptional(Object obj) {
        if (obj instanceof Optional) {
            return ((Optional<?>) obj).orElse(null);
        }
        return obj;
    }

    public static String getOptionalString(Object obj) {
        Object resolved = resolveOptional(obj);
        return resolved != null ? resolved.toString() : null;
    }
}
