package org.enoch.snark.instance.model.types;

public enum Expression {
    ALL,
    NONE,
    UNKNOWN;

    public boolean is(String value) {
        return this.name().toLowerCase().equals(value.trim().toLowerCase());
    }
}
