package org.enoch.snark.instance.model.to;

import lombok.Getter;
import org.enoch.snark.db.entity.ColonyEntity;

public class Target {

    public static final String PLANET = "planet";
    public static final String MOON = "moon";

    public static final String SWAP = "swap";
    public static final String NEXT = "next";
    public static final String FIND = "find";
    public static final String PREV = "prev";
    public static final String MAIN_FLEET_TO = "main_fleet_to";
    public static final String MAIN_FLEET_ON = "main_fleet_on";
    public static final String MAIN_FLEET = "main_fleet";

    public static final String PROBE_SWAM = "probe_swam";

    public static final String FARM = "farm";
    public static final String EXPRESSION_SEPARATOR = "-";

    @Getter
    private String action;
    @Getter
    private Planet planet;

    public static Target parse(String marker, ColonyEntity colony) {
        return new Target(marker, colony.toPlanet());
    }

    private Target(String action, Planet planet ) {
        this.action = action;
        this.planet = planet;
    }
}
