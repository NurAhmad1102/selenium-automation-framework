package org.example.utils.actionUtils;

import com.google.gson.Gson;
import org.example.config.Config;
import org.example.utils.resource.ResourcePathResolver;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonReader {
    private static Config config;

    public static synchronized Config getConfig(){
        if(config == null){
            config = loadConfigData();
        }
        return config;
    }

    private static Config loadConfigData(){
        try {
            Path configPath = ResourcePathResolver.resolve(
                    "data/backoffice/slaStatus/data.json",
                    "src/test/resources/data/backoffice/slaStatus/data.json",
                    "data/data.json",
                    "src/test/resources/data/data.json",
                    "src/test/java/resources/data/data.json"
            );
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                Gson gson = new Gson();
                return gson.fromJson(reader, Config.class);
            }
        } catch (Exception e){
            throw new RuntimeException("JsonReader failed to load configuration: " + e.getMessage(), e);
        }
    }
}

