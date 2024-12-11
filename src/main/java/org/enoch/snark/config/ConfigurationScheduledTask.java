package org.enoch.snark.config;

import org.enoch.snark.instance.Core;
import org.enoch.snark.instance.DynamicTaskManager;
import org.enoch.snark.instance.si.module.ConfigMap;
import org.enoch.snark.instance.si.module.ModuleMap;
import org.enoch.snark.instance.si.module.PropertiesMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.enoch.snark.instance.si.module.ConfigMap.GLOBAL;

@Component
public class ConfigurationScheduledTask {

    public static final String TEMPLATE = "template";
    private static List<ConfigTerm> configs = new ArrayList<>();

    private final DynamicTaskManager core;

    @Value("${properties:server.properties}")
    private String propertiesPath;

    public ConfigurationScheduledTask(DynamicTaskManager core) {
        this.core = core;
    }

    @Scheduled(fixedDelay = 10000)
    public void loadConfig() throws IOException {
        List<ConfigTerm> globals = loadConfigsFromFile(propertiesPath, GLOBAL);
        List<ConfigTerm> result = new ArrayList<>(globals);
        for (ConfigTerm term : globals)
            if(TEMPLATE.equals(term.getKey())) {
                result.addAll(loadConfigsFromFile(template(term.getValue()), term.getModule()));
            }
        if (areConfigsChanged(configs, result)) {
            result.forEach(System.err::println);
            configs = result;
            System.err.println("-----------------------------------------------");
            core.configurationUpdate(buildPropertiesMap());
        }
    }

    private boolean areConfigsChanged(List<ConfigTerm> configs, List<ConfigTerm> result) {
        if(configs.size() != result.size()) return true;
        for(int i = 0; i < configs.size(); i++)
            if (!configs.get(i).getValue().equals(result.get(i).getValue()))
                return true;
        return false;
    }

    public static PropertiesMap buildPropertiesMap() {
        PropertiesMap propertiesMap = new PropertiesMap();
        for(ConfigTerm term : configs) {
            propertiesMap.putIfAbsent(term.getModule(), new ModuleMap());
            ModuleMap moduleMap = propertiesMap.get(term.getModule());

            moduleMap.putIfAbsent(term.getThread(), new ConfigMap());
            ConfigMap threadMap = moduleMap.get(term.getThread());

            threadMap.putIfAbsent(ConfigMap.NAME, term.getThread());
            threadMap.putIfAbsent(ConfigMap.MODULE, term.getModule());
            threadMap.putIfAbsent(term.getKey(), term.getValue());
        }
        return propertiesMap;
    }

    private static List<ConfigTerm> loadConfigsFromFile(String propertiesPath, String defaultModule) throws IOException {
        File file = new File(propertiesPath);
        if(!file.exists() || !file.isFile()) throw new RuntimeException("File "+file.getAbsolutePath()+" is incorrect");

        FileInputStream fileInputStream = new FileInputStream(file);
        Properties properties = new Properties();
        properties.load(fileInputStream);
        fileInputStream.close();
        return properties.stringPropertyNames().stream()
                .map(s -> ConfigTerm.parse(s, properties.getProperty(s), defaultModule)).toList();
    }

    private String template(String module) {
        return TEMPLATE+File.separator+module+".properties";
    }
}

