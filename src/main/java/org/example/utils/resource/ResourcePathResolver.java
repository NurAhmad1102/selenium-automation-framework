package org.example.utils.resource;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class ResourcePathResolver {

    private ResourcePathResolver() {
    }

    public static Path resolve(String resourcePath, String... fallbackPaths) {
        String normalizedResourcePath = normalizeResourcePath(resourcePath);
        URL resourceUrl = getClassLoader().getResource(normalizedResourcePath);

        if (resourceUrl != null && "file".equalsIgnoreCase(resourceUrl.getProtocol())) {
            try {
                return Paths.get(resourceUrl.toURI());
            } catch (URISyntaxException e) {
                throw new IllegalArgumentException("Invalid resource URI: " + normalizedResourcePath, e);
            }
        }

        for (String fallbackPath : fallbackPaths) {
            Path candidate = Paths.get(fallbackPath);
            if (!candidate.isAbsolute()) {
                candidate = Paths.get(System.getProperty("user.dir")).resolve(candidate);
            }
            if (Files.exists(candidate)) {
                return candidate;
            }
        }

        if (resourceUrl != null) {
            throw new IllegalStateException(
                    "Resource exists but is not available as a file-system path: " + normalizedResourcePath);
        }

        throw new IllegalArgumentException("Resource not found: " + normalizedResourcePath);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        return contextClassLoader != null ? contextClassLoader : ResourcePathResolver.class.getClassLoader();
    }

    private static String normalizeResourcePath(String resourcePath) {
        if (resourcePath == null || resourcePath.isBlank()) {
            throw new IllegalArgumentException("Resource path must not be blank");
        }
        return resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
    }
}
