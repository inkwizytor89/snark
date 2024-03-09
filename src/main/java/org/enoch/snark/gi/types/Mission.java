package org.enoch.snark.gi.types;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;

public enum Mission {
    EXPEDITION("id=missionButton15", 15),
    COLONIZATION("id=missionButton7", 7),
    RECYCLE("id=missionButton8", 8),
    TRANSPORT("id=missionButton3", 3),
    STATIONED("id=missionButton4", 4),
    SPY("id=missionButton6", 6),
    STOP("id=missionButton5", 5),
    ATTACK("id=missionButton1", 1),
    GROUP_ATTACK("id=missionButton2", 2),
    DESTROY("id=missionButton9", 9),
    LIFE_FORM("LIFE_FORM", 0),
    UNKNOWN("UNKNOWN",0);

    private String id;
    private int value;

    Mission(String id, int value) {
        this.id = id;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public static Mission convertFromString(@Nonnull String name) {
        for (Mission mission : allMissions()) {
            if(mission.name().equals(name)) {
                return mission;
            }
        }
        throw new RuntimeException("Unknown mission " + name);
    }

    public static Mission convert(String input) {
        String string = input.toLowerCase();
        if(string.contains(EXPEDITION.name().toLowerCase()) || string.contains("ekspedycja")) {
            return EXPEDITION;
        } else if(string.contains(STATIONED.name().toLowerCase()) || string.contains("stacjonuj")) {
            return STATIONED;
        } else if(string.contains(TRANSPORT.name().toLowerCase()) || string.contains("transportuj")) {
            return TRANSPORT;
        } else if(string.contains(COLONIZATION.name().toLowerCase()) || string.contains("oloniz")) {
            return COLONIZATION;
        } else if(string.contains(RECYCLE.name().toLowerCase()) || string.contains("recykluj")) {
            return RECYCLE;
        }  else if(string.contains(ATTACK.name().toLowerCase()) || string.contains("atakuj")) {
            return ATTACK;
        } else if(string.contains(SPY.name().toLowerCase()) || string.contains("szpieguj")) {
            return SPY;
        } else if(string.contains(LIFE_FORM.name().toLowerCase()) || string.contains("form")) {
            return LIFE_FORM;
        } else if(string.contains(GROUP_ATTACK.name().toLowerCase()) || string.contains("atak") || string.contains("group")) {
            return GROUP_ATTACK;
        } else return UNKNOWN;
    }

    public boolean isAggressive() {
        return this.equals(SPY) || this.equals(ATTACK) || this.equals(DESTROY) || this.equals(GROUP_ATTACK);
    }

    public boolean isNotComingBack() {
        return Arrays.asList(STATIONED, COLONIZATION).contains(this);
    }

    public boolean isComingBack() {
        return !isNotComingBack();
    }

    public boolean isIn(Mission... missions) {
        return Arrays.asList(missions).contains(this);
    }

    static List<Mission> allMissions() {
        return Arrays.asList(EXPEDITION, COLONIZATION, RECYCLE, TRANSPORT, STATIONED, SPY, STOP, ATTACK, GROUP_ATTACK, DESTROY);
    }
}
