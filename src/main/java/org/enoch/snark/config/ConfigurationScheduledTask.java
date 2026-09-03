package org.enoch.snark.config;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.module.ThreadMap;
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

import static org.enoch.snark.instance.si.module.ThreadMap.*;

@Component
@RequiredArgsConstructor
public class ConfigurationScheduledTask {

    public static final String TEMPLATE = "template";
    private static List<ConfigTerm> configs = new ArrayList<>();

    @Value("${properties:server.properties}")
    private String propertiesPath;

    public List<ConfigTerm>  loadConfig() throws IOException {
        List<ConfigTerm> globals = loadConfigsFromFile(propertiesPath, GLOBAL);
        List<ConfigTerm> result = new ArrayList<>(globals);
        for (ConfigTerm term : globals)
            if(TEMPLATE.equals(term.getKey()) && !StringUtils.isEmpty(term.getValue())) {
                result.addAll(loadConfigsFromFile(term.getValue(), term.getModule()));
            }
        if (areConfigsChanged(configs, result)) {
            configs = result;
            return new ArrayList<>(configs);
        } else return null;
    }

    private boolean areConfigsChanged(List<ConfigTerm> configs, List<ConfigTerm> result) {
        if(configs.size() != result.size()) return true;
        for(int i = 0; i < configs.size(); i++)
            if (!configs.get(i).getValue().equals(result.get(i).getValue()))
                return true;
        return false;
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

    public String determineDatabase() {
        return getFirstByKey(SERVER).getValue();
    }

    private ConfigTerm getFirstByKey(String key) {
        for(ConfigTerm term : configs) {
            if(key.equals(term.getKey())) {
               return term;
            }
        }
        return null;
    }
}

