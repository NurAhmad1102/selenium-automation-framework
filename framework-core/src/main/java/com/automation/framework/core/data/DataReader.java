package com.automation.framework.core.data;

import java.nio.file.Path;

/**
 * Generic interface for test data providers and readers.
 *
 * @param <T> Target data model type
 */
public interface DataReader<T> {

    T read(Path path);

    default T read(String resourcePath, String... fallbackPaths) {
        Path resolved = ResourcePathResolver.resolve(resourcePath, fallbackPaths);
        return read(resolved);
    }
}
