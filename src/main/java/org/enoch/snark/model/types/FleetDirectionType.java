package org.enoch.snark.model.types;

public enum FleetDirectionType {
    THERE("THERE"),
    BACK("BACK");

    private String name;

    FleetDirectionType(String name) {
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
