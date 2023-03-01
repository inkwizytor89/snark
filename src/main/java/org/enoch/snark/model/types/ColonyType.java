package org.enoch.snark.model.types;

public enum ColonyType {
    PLANET,
    MOON,
    DEBRIS;

    public static ColonyType parse(String spaceTarget) {
        if(MOON.name().equals(spaceTarget)) {
            return MOON;
        } else if(DEBRIS.name().equals(spaceTarget)) {
            return DEBRIS;
        } else return PLANET;
    }
}
