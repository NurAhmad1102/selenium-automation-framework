package com.automation.framework.core.data;

import com.google.gson.Gson;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Agnostic JSON deserializer for POJO test data models.
 */
public class JsonDataReader<T> implements DataReader<T> {

    private final Class<T> targetClass;
    private final Gson gson = new Gson();

    public JsonDataReader(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    @Override
    public T read(Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON from " + path + ": " + e.getMessage(), e);
        }
    }

    public static <T> T fromJson(String resourcePath, Class<T> targetClass, String... fallbacks) {
        return new JsonDataReader<>(targetClass).read(resourcePath, fallbacks);
    }
}
