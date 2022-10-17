package org.enoch.snark.model.types;

public enum MissionType {
    SPY("SPY"),
    EXPEDITION("EXPEDITION"),
    COLONIZE("COLONIZE"),
    RECYCLE("RECYCLE"),
    STATION("STATION"),
    TRANSPORT("TRANSPORT"),
    ATTACK("ATTACK"),
    UNKNOWN("UNKNOWN");

    private String name;

    MissionType(String name) {
        this.name = name;
    }

    public static MissionType convert(String input) {
        String string = input.toLowerCase();
        if(string.contains(EXPEDITION.getName().toLowerCase()) || string.contains("ekspedycja")) {
            return EXPEDITION;
        } else if(string.contains(STATION.getName().toLowerCase()) || string.contains("stacjonuj")) {
            return STATION;
        } else if(string.contains(TRANSPORT.getName().toLowerCase()) || string.contains("transportuj")) {
            return TRANSPORT;
        } else if(string.contains(COLONIZE.getName().toLowerCase()) || string.contains("kolonizuj")) {
            return COLONIZE;
        } else if(string.contains(RECYCLE.getName().toLowerCase()) || string.contains("recykluj")) {
            return RECYCLE;
        }  else if(string.contains(ATTACK.getName().toLowerCase()) || string.contains("atakuj")) {
            return ATTACK;
        } else if(string.contains(SPY.getName().toLowerCase()) || string.contains("szpieguj")) {
            return SPY;
        } else return UNKNOWN;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
