package org.enoch.snark.model.types;

public enum ColonyType {
    PLANET,
    MOON,
    DEBRIS,
    UNKNOWN;

    public static ColonyType parse(String spaceTarget) {
        if(MOON.name().equals(spaceTarget)) {
            return MOON;
        } else if(DEBRIS.name().equals(spaceTarget)) {
            return DEBRIS;
        } else return PLANET;
    }

    public String code() {
        if (name().equals("MOON")) return "m";
        if (name().equals("PLANET")) return "p";
        if (name().equals("DEBRIS")) return "d";
        return "u";
    }
}
