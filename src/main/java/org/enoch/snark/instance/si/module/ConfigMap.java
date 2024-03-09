package org.enoch.snark.instance.si.module;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ConfigMap extends HashMap<String, String> {

    public static final String MAIN = "main";

    public static final String SERVER = "server";
    public static final String URL = "url";
    public static final String LOGIN = "login";
    public static final String PASSWORD = "password";
    public static final String MODE = "mode";
    public static final String CONFIG = "config";
    public static final String WEBDRIVER_PATH = "driver";

    public static final String TIME = "time";
    public static final String TAG = "tag";
    public static final String PAUSE = "pause";
    public static final String ON = "on";
    public static final String OFF = "off";
    public static final String STOP = "stop";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    public static final String FLY_POINTS = "fly_points";
    public static final String SOURCE = "source";
    public static final String SHIPS_WAVE = "ships_wave";
    public static final String MISSION = "mission";
    public static final String RESOURCES = "resources";
    public static final String SPEED = "speed";
    public static final String EXPLORATION_AREA = "exploration_area";
    public static final String DATABASE = "database";
    public static final String GALAXY_MAX = "galaxy_max";
    public static final String SYSTEM_MAX = "system_max";
    public static final String HIDING_ACTIVITY = "hiding_activity";
    public static final String HIGH_SCORE_PAGES = "high_score_pages";
    public static final String MASTER = "master_href";
    public static final String TRANSPORTER_SMALL_CAPACITY = "transporterSmall";
    public static final String LEAVE_MIN_RESOURCES = "leave_min_resources";

    public ConfigMap() {
        super();
    }

    public ConfigMap(Map map) {
        super(map);
    }

    public String getConfig(String key) {
        return getConfig(key, "missing_value");
    }

    public String getConfig(String key, String defaultValue) {
        if(!containsKey(key)) {
            return defaultValue;
        }
        String value = get(key);
        if(value.trim().isEmpty()) return defaultValue;
        return value;
    }

    public String getNearestConfig(String key, String defaultValue) {
        if(this.containsKey(key)) return this.get(key);
        if(Instance.getMainConfigMap().containsKey(key)) return Instance.getMainConfigMap().get(key);
        return defaultValue;
    }

    public Boolean getConfigBoolean(String key, Boolean defaultValue) {
        return Boolean.parseBoolean(getConfig(key, defaultValue.toString()));
    }

    public Integer getConfigInteger(String key, Integer defaultValue) {
        try {
            return Integer.parseInt(getConfig(key, defaultValue.toString()));
        } catch (NumberFormatException e) {
            System.err.println("For "+get(NAME)+" key "+key+" get default value " + defaultValue +
                    " because "+e.getMessage());
            return defaultValue;
        }
    }

    public Long getConfigLong(String key, Long defaultValue) {
        try {
            return Long.parseLong(getConfig(key, defaultValue.toString()));
        } catch (NumberFormatException e) {
            System.err.println("For "+get(NAME)+" key "+key+" get default value " + defaultValue +
                    " because "+e.getMessage());
            return defaultValue;
        }
    }

    public String[] getConfigArray(String key) {
        return get(key).split(";");
    }

    public boolean isOn() {
        String[] configArray = getConfigArray(TIME);
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

    public Planet getConfigPlanet(String key) {
        String config = getConfig(key, null);
        if (config == null) return null;
        return new Planet(config);
    }

    public Resources getConfigResource(String key, Resources defaultResources) {
        String config = getConfig(key, null);
        if (config == null) return defaultResources;
        return new Resources(config);
    }

    public String name() {
        if(!this.containsKey(NAME)) {
            System.err.println("Missing name in ConfigMap");
            put(NAME, "Missing name in ConfigMap");
        }
        return this.get(NAME);
    }

    public List<ColonyEntity> getFlyPoints() {
        return ColonyDAO.getInstance().getColonies(getNearestConfig(FLY_POINTS, StringUtils.EMPTY));
    }

    public List<ColonyEntity> getColonies(String key, String defaultValue) {
        return ColonyDAO.getInstance().getColonies(getConfig(key, defaultValue));
    }

    public List<Map<ShipEnum,Long>> getShipsWaves() {
        List<Map<ShipEnum, Long>> empty = Collections.singletonList(new HashMap<>());
        if(!keySet().contains(SHIPS_WAVE)) return empty;
        String[] shipsWavesArray = getConfigArray(SHIPS_WAVE);
        if(shipsWavesArray.length == 0) return empty;
        ArrayList<Map<ShipEnum,Long>> waves = new ArrayList<>();
        for(String waveString : shipsWavesArray) {
            waves.add(ShipEnum.parse(waveString));
        }
        return waves;
    }
}
