package org.enoch.snark.instance.config;

import org.enoch.snark.instance.si.module.ThreadMap;
import org.enoch.snark.instance.si.module.PropertiesMap;
import org.enoch.snark.instance.si.module.ModuleMap;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import static org.enoch.snark.instance.si.module.ThreadMap.GLOBAL;

@Deprecated
public class ConfigReader {

    public static String configPath = "server.properties";

    public static void setConfigPath(String path) {
        configPath = path;
    }

    public synchronized static PropertiesMap load() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(configPath);
        Properties properties = new java.util.Properties();
        properties.load(fileInputStream);
        fileInputStream.close();

        return buildPropertiesMap(properties);
    }

    public static PropertiesMap buildPropertiesMap(Properties properties) {
        PropertiesMap propertiesMap = new PropertiesMap();
        for(String propertyString : properties.stringPropertyNames()) {
            String[] elements = propertyString.split("\\.");
            int elementsLength = elements.length;

            String module = elementsLength == 3 ? elements[elementsLength-3] : GLOBAL;
            String name = elements[elementsLength-2];
            String key = elements[elementsLength-1];
            String value = properties.getProperty(propertyString);

            propertiesMap.putIfAbsent(module, new ModuleMap());
            ModuleMap moduleMap = propertiesMap.get(module);


            moduleMap.putIfAbsent(name, new ThreadMap());
            ThreadMap threadMap = moduleMap.get(name);

            threadMap.putIfAbsent(ThreadMap.NAME, name);
            threadMap.putIfAbsent(ThreadMap.MODULE, module);
            threadMap.put(key, value);
        }
        return propertiesMap;
    }
}
