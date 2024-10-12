package org.enoch.snark.instance.si.module;

import org.enoch.snark.instance.config.ConfigReader;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.si.module.ConfigMap.*;
import static org.enoch.snark.instance.si.module.ConfigMap.TIME;

public abstract class PropertiesModule extends AbstractModule{

    public PropertiesModule(ModuleMap moduleMap) {
        super(moduleMap);
    }

    protected ModuleMap createBaseMap() {
        Properties properties = new Properties();
        Reader targetReader = new StringReader(getProperties());
        try {
            properties.load(targetReader);
            targetReader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        PropertiesMap propertiesMap = ConfigReader.buildPropertiesMap(properties);
        return propertiesMap.get(GLOBAL);
    }

    protected abstract String getProperties();

    @Override
    public void updateMap(ModuleMap moduleMap) {
        overrideFromMain(moduleMap);
//        overrideThreadsTimeFromMainTime(moduleMap);
        super.updateMap(moduleMap);
    }

    protected void overrideThreadsTimeFromMainTime(ModuleMap moduleMap) {
        if(moduleMap.containsKey(MAIN)) {
            ConfigMap mainConfigMap = moduleMap.get(MAIN);
            if(mainConfigMap.containsKey(TIME)) {
                String time = mainConfigMap.get(TIME);
                if(time.contains(ON) || time.contains(OFF)) {
                    threadsMap.keySet().forEach(name -> {
                        if(moduleMap.containsKey(name)) moduleMap.get(name).put(TIME, time);
                        else {
                            ConfigMap configMap = new ConfigMap();
                            configMap.put(TIME, time);
                            moduleMap.put(name, configMap);
                        }
                    });
                }
            }
        }
    }

    protected void overrideFromMain(ModuleMap moduleMap) {
        Collection<String> keySet = extractKeysFromMainConfigMaps(moduleMap);
        keySet.forEach(key -> {
            String value = moduleMap.get(MAIN).get(key);
            threadsMap.keySet().forEach(name -> {
                if(moduleMap.containsKey(name)) moduleMap.get(name).put(key, value);
                else {
                    ConfigMap configMap = new ConfigMap();
                    configMap.put(key, value);
                    moduleMap.put(name, configMap);
                }
            });
        });
    }

    private static Collection<String> extractKeysFromMainConfigMaps(ModuleMap moduleMap) {
        Collection<String> keys = moduleMap.containsKey(MAIN) ? moduleMap.get(MAIN).keySet() : new ArrayList<>();
        return keys.stream()
                .filter(key -> !NAME.equals(key))
                .filter(key -> !TIME.equals(key))
                .toList();
    }
}
