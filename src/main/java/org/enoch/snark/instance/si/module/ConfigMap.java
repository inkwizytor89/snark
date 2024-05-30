package org.enoch.snark.instance.si.module;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

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
    public static final String ARRAY_SEPARATOR = ";";
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");

    public static final String DEBUG = "debug";
    public static final String DRY_RUN = "dry_run";
    public static final String SOURCE = "source";
    public static final String TARGET = "target";
    public static final String CONDITION_RESOURCES_COUNT = "condition_resources_count";
    public static final String CONDITION_RESOURCES = "condition_resources";
    public static final String CONDITION_SHIPS = "condition_ships";
    public static final String CONDITION_BLOCKING_MISSIONS = "condition_blocking_missions";
    public static final String SHIPS_WAVE = "ships_wave";
    public static final String LEAVE_SHIPS_WAVE = "leave_ships_wave";
    public static final String MISSION = "mission";
    public static final String RESOURCES = "resources";
    public static final String LEAVE_RESOURCES = "leave_resources";
    public static final String SPEED = "speed";
    public static final String QUEUE = "queue";
    public static final String EXPIRED_TIME = "expired_time";
    public static final String EXPLORATION_AREA = "exploration_area";
    public static final String DATABASE = "database";
    public static final String GALAXY_MAX = "galaxy_max";
    public static final String SYSTEM_MAX = "system_max";
    public static final String PAGE_SIZE = "page_size";
    public static final String HIDING_ACTIVITY = "hiding_activity";
    public static final String HIGH_SCORE_PAGES = "high_score_pages";
    public static final String MASTER = "master_href";
    public static final String TRIP = "trip";
    public static final String TRANSPORTER_SMALL_CAPACITY = "transporterSmall";
    public static final String LEAVE_MIN_RESOURCES = "leave_min_resources";
    public static final String PROBE_SWAM_LIMIT = "probe_swam_limit";

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

    public LocalTime getLocalTime(String key, LocalTime defaultValue) {
        if(!containsKey(key)) return defaultValue;
        return LocalTime.parse(getConfig(key), dtf);
    }

    public Long getConfigLong(String key, Long defaultValue) {
        try {
            String defaultString = defaultValue == null ? null : defaultValue.toString();
            String config = getConfig(key, defaultString);
            return config == null ? null : Long.parseLong(config);
        } catch (NumberFormatException e) {
            System.err.println("For "+get(NAME)+" key "+key+" get default value " + defaultValue +
                    " because "+e.getMessage());
            return defaultValue;
        }
    }

    public Long getConfigNumber(String key, String defaultValue) {
        String config = getConfig(key, defaultValue);
        return config == null ? null : NumberUtil.toLong(config);
    }

    public List<String> getConfigArray(String key) {
        if(!containsKey(key)) return new ArrayList<>();
        return Arrays.asList(get(key).split(ARRAY_SEPARATOR));
    }

    public List<String> getConfigArray(String key, List<String> defaultValue) {
        if(!containsKey(key)) return defaultValue;
        return Arrays.asList(get(key).split(ARRAY_SEPARATOR));
    }

    public boolean isOn() {
        List<String> configArray = getConfigArray(TIME);
        if(configArray == null || configArray.size() == 0)  return true;
        if(configArray.size() == 1 && configArray.get(0).contains(ON))  return true;
        if(configArray.size() == 1 && configArray.get(0).contains(OFF))  return false;
        if(configArray.size() == 1 && configArray.get(0).equals(StringUtils.EMPTY))  return false;

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

    public Resources getConfigResources(String key, Resources defaultResources) {
        String config = getConfig(key, null);
        if (config == null) return defaultResources;
        return Resources.parse(config);
    }

    public String name() {
        if(!this.containsKey(NAME)) {
            System.err.println("Missing name in ConfigMap");
            put(NAME, "Missing name in ConfigMap");
        }
        return this.get(NAME);
    }

    public List<ColonyEntity> getSources() {
        return ColonyDAO.getInstance().getColonies(getNearestConfig(SOURCE, StringUtils.EMPTY));
    }

    public List<ColonyEntity> getColonies(String key, String defaultValue) {
        return ColonyDAO.getInstance().getColonies(getConfig(key, defaultValue));
    }
    public ShipsMap getShips(String code, ShipsMap defaultValue) {
        String ships = getConfig(code, null);
        if(StringUtils.isEmpty(ships)) return defaultValue;
        return ShipsMap.parse(ships);
    }

    public List<ShipsMap> getShipsWaves(List<ShipsMap> defaultList) {
        return getShipsWaves(SHIPS_WAVE, defaultList);
    }

    public List<ShipsMap> getShipsWaves(String code, List<ShipsMap> defaultList) {
        List<String> shipsWavesList = getConfigArray(code);

        if(keySet().contains(code) && !shipsWavesList.isEmpty()) {
            List<ShipsMap> result = new ArrayList<>();
            shipsWavesList.forEach(wave -> result.add(ShipsMap.parse(wave)));
            return result;
        }
        return defaultList;
    }

    @Override
    public String toString() {
        return "{"+this.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining(", "))+"}";
    }
}
