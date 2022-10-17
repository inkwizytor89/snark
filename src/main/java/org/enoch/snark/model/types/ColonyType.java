package org.enoch.snark.model.types;

public enum ColonyType {
    PLANET("PLANET"),
    MOON("MOON");

    private String name;

    private ColonyType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
