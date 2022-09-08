package org.enoch.snark.model;

public class Resources {

    public static Resources unknown = new Resources(-1L, -1L, -1L);

    public Long metal = 0L;
    public Long crystal = 0L;
    public Long deuterium = 0L;

    public Resources() {

    }

    public Resources(long metal, long crystal, long deuterium) {
        this.metal = metal;
        this.crystal = crystal;
        this.deuterium = deuterium;
    }

    @Override
    public String toString() {
        return "{" + metal + ", " + crystal + ", " + deuterium +"}";
    }
}
