package org.enoch.snark.model;

import org.enoch.snark.instance.AppProperties;

public class Universe {


    public static final String DATABASE = "database";
    public static final String GALAXY_MAX = "galaxy_max";
    public static final String SYSTEM_MAX = "system_max";
    public static final String EXPLORATION_AREA = "exploration_area";
    public static final String MIN_DT = "min_dt";
    public static final String MAX_DT = "max_dt";
    public static final String MASTER = "master_href";
    public static final String FLY_POINTS = "fly_points";
    public static final String DEFENSE = "defense";


    public String name;
    public String url;

    public String login;
    public String pass;

    public String mode;
    public String config;

    public static Universe loadProperties(AppProperties appProperties) {
        Universe universe = new Universe();
        universe.name = appProperties.server;
        universe.url = appProperties.url;
        universe.login = appProperties.username;
        universe.pass = appProperties.password;
        universe.mode = appProperties.mode;
        universe.config = appProperties.config;
        return universe;
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
}
