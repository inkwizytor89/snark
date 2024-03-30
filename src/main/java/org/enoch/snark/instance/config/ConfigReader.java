package org.enoch.snark.instance.config;

import org.enoch.snark.instance.si.module.ConfigMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;

public class ConfigReader {

    public static String configPath = "server.properties";

    public static void setConfigPath(String path) {
        configPath = path;
    }

    public synchronized static HashMap<String, ConfigMap> load() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(configPath);
        Properties properties = new java.util.Properties();
        properties.load(fileInputStream);
        fileInputStream.close();

        return buildPropertiesMap(properties);
    }

    private static HashMap<String, ConfigMap> buildPropertiesMap(Properties properties) {
        HashMap<String, ConfigMap> globalMap = new HashMap<>();
        for(String propertyString : properties.stringPropertyNames()) {
            int index = propertyString.indexOf('.');
            String name = propertyString.substring(0, index);
            String key = propertyString.substring(index+1);
            String value = properties.getProperty(propertyString);

            if(!globalMap.containsKey(name)){
                globalMap.put(name, new ConfigMap());
            }
            ConfigMap threadMap = globalMap.get(name);
            threadMap.put(key, value);
        }
        globalMap.forEach((s, map) -> map.put(ConfigMap.NAME, s));
        return globalMap;
    }
}
