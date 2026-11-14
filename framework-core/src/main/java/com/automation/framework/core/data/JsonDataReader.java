package com.automation.framework.core.data;

import com.google.gson.Gson;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Agnostic JSON deserializer for POJO test data models.
 *
 * @param <T> Target POJO model class
 */
public class JsonDataReader<T> implements DataReader<T> {

    private final Class<T> targetClass;
    private final Gson gson = new Gson();

    /**
     * Constructs a JsonDataReader for the specified model class.
     *
     * @param targetClass class type to deserialize into
     */
    public JsonDataReader(Class<T> targetClass) {
        this.targetClass = targetClass;
    }

    /**
     * Reads and deserializes JSON content from the given file path into target POJO.
     *
     * @param path file path to JSON file
     * @return deserialized instance of T
     * @throws RuntimeException if file reading or deserialization fails
     */
    @Override
    public T read(Path path) {
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read JSON from " + path + ": " + e.getMessage(), e);
        }
    }

    /**
     * Convenience static helper to read a JSON resource directly into a POJO model.
     *
     * @param resourcePath classpath or relative file path to the JSON file
     * @param targetClass target model class
     * @param fallbacks optional fallback file paths
     * @param <T> target type
     * @return deserialized instance of T
     */
    public static <T> T fromJson(String resourcePath, Class<T> targetClass, String... fallbacks) {
        return new JsonDataReader<>(targetClass).read(resourcePath, fallbacks);
    }
}
