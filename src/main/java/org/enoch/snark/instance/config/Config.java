package org.enoch.snark.instance.config;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.module.building.BuildingThread;
import org.enoch.snark.module.clear.ClearThread;
import org.enoch.snark.module.collector.CollectorThread;
import org.enoch.snark.module.defense.DefenseThread;
import org.enoch.snark.module.expedition.ExpeditionThread;
import org.enoch.snark.module.farm.FarmThread;
import org.enoch.snark.module.fleetSave.FleetSaveThread;
import org.enoch.snark.module.scan.ScanThread;
import org.enoch.snark.module.space.SpaceThread;
import org.enoch.snark.module.update.UpdateThread;

import java.util.Properties;

public class Config {

    public static final String SERVER = "server";
    public static final String URL = "url";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String MODE = "mode";
    public static final String CONFIG = "config";
    public static final String WEBDRIVER_CHROME_DRIVER = "webdriver.chrome.driver";

    public static final String FLY_POINTS = "fly_points";
    public static final String EXPLORATION_AREA = "exploration_area";
    public static final String DATABASE = "database";
    public static final String GALAXY_MAX = "galaxy_max";
    public static final String SYSTEM_MAX = "system_max";
    public static final String MASTER = "master_href";

    public String name;
    public String url;
    public String login;
    public String pass;
    public String mode;
    public String config;

    public String defense;
    public String expedition;
    public String farm;
    public String collector;
    public String building;
    public String fleetSave;
    public String space;
    public String scan;

    public String update;
    public String clear;

    public String pathToChromeWebdriver;

    public Config(Properties properties) {

        url = properties.getProperty(URL);
        name = properties.getProperty(SERVER);
        login = properties.getProperty(LOGIN);
        pass = properties.getProperty(PASSWORD);

        mode = properties.getProperty(MODE);
        config = properties.getProperty(CONFIG);

        defense = properties.getProperty(DefenseThread.threadName);
        expedition = properties.getProperty(ExpeditionThread.threadName);
        farm = properties.getProperty(FarmThread.threadName);
        collector = properties.getProperty(CollectorThread.threadName);
        building = properties.getProperty(BuildingThread.threadName);
        fleetSave = properties.getProperty(FleetSaveThread.threadName);
        space = properties.getProperty(SpaceThread.threadName);
        scan = properties.getProperty(ScanThread.threadName);

        update = properties.getProperty(UpdateThread.threadName);
        clear = properties.getProperty(ClearThread.threadName);

        pathToChromeWebdriver = properties.getProperty(WEBDRIVER_CHROME_DRIVER);
        System.setProperty(WEBDRIVER_CHROME_DRIVER, pathToChromeWebdriver);

    }

    public String getConfig(String key) {
        String[] configs = this.config.split(",");
        for (String s : configs) {
            String[] configuration = s.split("=");
            if (key.equals(configuration[0])) {
                return configuration.length > 1 ? configuration[1] : "";
            }
        }
        return null;
    }

    public String get(String threadName, String key) {
        String threadConfig = extract(threadName);
        if (threadConfig == null) return "";
        String[] configs = threadConfig.split(",");
        for (String s : configs) {
            String[] configuration = s.split("=");
            if (key.equals(configuration[0])) {
                return configuration.length > 1 ? configuration[1] : StringUtils.EMPTY;
            }
        }
        return StringUtils.EMPTY;
    }

    public boolean contains(String threadName, String key) {
        return extract(threadName).contains(key);
    }

    private String extract(String threadName) {
        switch (threadName) {
            case DefenseThread.threadName: return defense;
            case ExpeditionThread.threadName: return expedition;
            case FarmThread.threadName: return farm;
            case CollectorThread.threadName: return collector;
            case BuildingThread.threadName: return building;
            case FleetSaveThread.threadName: return fleetSave;
            case SpaceThread.threadName: return space;
            case ScanThread.threadName: return scan;
            case UpdateThread.threadName: return update;
            case ClearThread.threadName: return clear;
            default: return config;
        }
    }
}
