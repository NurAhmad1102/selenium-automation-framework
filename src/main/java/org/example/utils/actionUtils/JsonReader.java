package org.example.utils.actionUtils;

import com.google.gson.Gson;
import org.example.config.Config;

import java.io.FileReader;
import java.io.IOException;

public class JsonReader {
    private static Config config;

    public static Config getConfig(){
        if(config == null){
            config = loadConfigData();
        }
        return config;
    }

    private static Config loadConfigData(){
      try{
          FileReader reader = new FileReader(
                  System.getProperty("user.dir")+"/src/test/java/resources/data/data.json"
          );

          Gson gson = new Gson();
          return gson.fromJson(reader, Config.class);
      } catch (IOException e){
          throw new RuntimeException("JsonReader "+e);
      }
    }
}

