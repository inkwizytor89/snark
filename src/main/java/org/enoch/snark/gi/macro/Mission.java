package org.enoch.snark.gi.macro;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

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
    DESTROY("id=missionButton9", 9);

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

    static List<Mission> allMissions() {
        return Arrays.asList(EXPEDITION, COLONIZATION, RECYCLE, TRANSPORT, STATIONED, SPY, STOP, ATTACK, GROUP_ATTACK, DESTROY);
    }
}
