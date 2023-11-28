package org.enoch.snark.instance.config;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.module.ConfigMap;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Properties;

public class Config {

    public static final String MAIN = "main";

    public static final String SERVER = "server";
    public static final String URL = "url";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String MODE = "mode";
    public static final String CONFIG = "config";
    public static final String WEBDRIVER_CHROME_DRIVER = "main.driver";

    public static final String TIME = "time";
    public static final String PAUSE = "pause";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String STOP = "stop";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    public static final String FLY_POINTS = "fly_points";
    public static final String EXPLORATION_AREA = "exploration_area";
    public static final String DATABASE = "database";
    public static final String GALAXY_MAX = "galaxy_max";
    public static final String SYSTEM_MAX = "system_max";
    public static final String HIDING_ACTIVITY = "hiding_activity";
    public static final String HIGH_SCORE_PAGES = "high_score_pages";
    public static final String MASTER = "master_href";

    private final Properties properties;

    public String pathToChromeWebdriver;

    public HashMap<String, ConfigMap> globalMap = new HashMap<>();

    public Config(Properties properties) {
        this.properties = properties;
        pathToChromeWebdriver = properties.getProperty(WEBDRIVER_CHROME_DRIVER);
        System.setProperty("webdriver.chrome.driver", pathToChromeWebdriver);

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
        globalMap.forEach((s, map) -> map.put(NAME, s));
    }

    public String getConfig(String key) {
        return getConfig(MAIN, key, "missing_value");
    }

    public String getConfig(String area, String key, String defaultValue) {
        String propertiesKey = createPropertiesKey(area, key);
        if(!properties.containsKey(propertiesKey)) {
//            System.err.println("Missing "+propertiesKey+" in properties file, set defaultValue '"+defaultValue+"'");
            return defaultValue;
        }
        String propertyValue = properties.getProperty(propertiesKey);
        if(propertyValue == null || StringUtils.EMPTY.equals(propertyValue)) {
            return defaultValue;
        }
        return propertyValue;
    }

    public String createPropertiesKey(String area, String key) {
        return area + "." + key;
    }

    public Integer getConfigInteger(String area, String key, Integer defaultValue) {
        try {
            return Integer.parseInt(getConfig(area, key, defaultValue.toString()));
        } catch (NumberFormatException e) {
            System.err.println("For "+createPropertiesKey(area, key)+" get default value " + defaultValue +
                    " because "+e.getMessage());
            return defaultValue;
        }
    }

    public Long getConfigLong(String area, String key, Long defaultValue) {
        try {
            return Long.parseLong(getConfig(area, key, defaultValue.toString()));
        } catch (NumberFormatException e) {
            System.err.println("For "+createPropertiesKey(area, key)+" get default value " + defaultValue +
                    " because "+e.getMessage());
            return defaultValue;
        }
    }

    public Boolean getConfigBoolean(String area, String key, Boolean defaultValue) {
        return Boolean.parseBoolean(getConfig(area, key, defaultValue.toString()));
    }

    public String[] getConfigArray(String area, String key) {
        return getConfig(area, key, "").split(";");
    }

    public boolean isOn(String area) {
        String[] configArray = Instance.config.getConfigArray(area, TIME);
        if(configArray == null || configArray.length == 0)  return true;
        if(configArray.length == 1 && configArray[0].contains(ON))  return true;
        if(configArray.length == 1 && configArray[0].contains(OFF))  return false;
        if(configArray.length == 1 && configArray[0].equals(StringUtils.EMPTY))  return false;

        for(String configTerm : configArray) {
            String[] vars = configTerm.split("-");
            if (vars.length == 2) {
                LocalTime start = LocalTime.parse(vars[0], dtf);
                LocalTime end = LocalTime.parse(vars[1], dtf);
                return (nowIsInMiddle(start, end) && start.isBefore(end)) ||
                        (!nowIsInMiddle(end, start) && start.isAfter(end));
            } else return false;
        }
        return false;
    }

    private boolean nowIsInMiddle(LocalTime start, LocalTime end) {
        LocalTime now = LocalTime.now();
        return now.isAfter(start) && now.isBefore(end);
    }
}
