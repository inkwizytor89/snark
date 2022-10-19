package org.enoch.snark.model.types;

public enum ColonyType {
    PLANET("PLANET"),
    MOON("MOON"),
    DEBRIS("debris");

    private String name;

    ColonyType(String name) {
        this.name = name;
    }

    public static ColonyType parse(String spaceTarget) {
        if(MOON.getName().equals(spaceTarget)) {
            return MOON;
        } else if(DEBRIS.getName().equals(spaceTarget)) {
            return DEBRIS;
        } else return PLANET;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
