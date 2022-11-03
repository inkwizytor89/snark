package org.enoch.snark.model;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.GalaxyEntity;
import org.enoch.snark.db.entity.IdEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.AppProperties;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

public class Universe {


    public static final String GALAXY_MAX = "galaxy_max";
    public static final String SYSTEM_MAX = "system_max";
    public static final String EXPLORATION_AREA = "exploration_area";
    public static final String MIN_DT = "min_dt";
    public static final String MAX_DT = "max_dt";
    public static final String MASTER = "master_href";
    public static final String FLY_POINTS = "fly_points";


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
        for(int i = 0; i<configs.length; i++) {
            String[] configurtion = configs[i].split("=");
            if(key.equals(configurtion[0])) {
                return configurtion.length>1 ? configurtion[1]: "";
            }
        }
        return null;
    }
}
