package org.enoch.snark.instance.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

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

    public static String configPath = "server.properties";

    public String name;
    public String url;

    public String login;
    public String pass;

    public String mode;
    public String config;

    public static void setConfigPath(String path) {
        configPath = path;
    }

    public synchronized static Config load() throws IOException {
        FileInputStream fileInputStream = new FileInputStream(configPath);
        Properties properties = new java.util.Properties();
        properties.load(fileInputStream);
        fileInputStream.close();

        return new Config(properties);


//        Universe universe = new Universe();
//        universe.name = config.server;
//        universe.url = config.url;
//        universe.login = config.username;
//        universe.pass = config.password;
//        universe.mode = config.mode;
//        universe.config = config.config;
//        return universe;
    }
}
