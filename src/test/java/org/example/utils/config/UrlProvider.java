package org.example.utils.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class UrlProvider {
    private static final Map<String, String> interfaceUrls = new ConcurrentHashMap<>(
            LoginDataRepository.getLoginData().getUrls());

    private UrlProvider() {
    }

    public static String getBackOfficeUrl() {
        return getUrl("Backoffice_staging");
    }

    public static String getFrontOfficeUrl() {
        return getUrl("Frontoffice_staging");
    }

    public static String getUrl(String interfaceType) {
        if (interfaceType == null) return null;
        String normalizedInterfaceType = interfaceType.trim().toLowerCase();
        String configuredUrl = interfaceUrls.get(normalizedInterfaceType);
        if (configuredUrl != null && !configuredUrl.isEmpty()) {
            return configuredUrl;
        }

        if (normalizedInterfaceType.contains("backoffice")) {
            configuredUrl = interfaceUrls.get("backoffice");
        } else if (normalizedInterfaceType.contains("frontoffice")) {
            configuredUrl = interfaceUrls.get("frontoffice");
        }
        if (configuredUrl != null && !configuredUrl.isEmpty()) {
            return configuredUrl;
        }
        return null;
    }
}
