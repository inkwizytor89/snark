package org.enoch.snark.model;

import org.enoch.snark.common.NumberUtil;

public class Resources {

    public static Resources unknown = new Resources(-1L, -1L, -1L);
    public static Resources everything = new Resources(-100L, -100L, -100L);

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

    /**
     * example: m4kk c4m d4.000.000
     *
     * @param input
     */
    public Resources(String input) {
        String[] resourcesStrings = input.trim().split("\\s+");
        for(String resourceString : resourcesStrings) {
            if (resourceString.startsWith("m")) {
                this.metal = NumberUtil.toLong(resourceString.substring(1));
            } else if(resourceString.startsWith("c")) {
                this.crystal = NumberUtil.toLong(resourceString.substring(1));
            } else if(resourceString.startsWith("d")) {
                this.deuterium = NumberUtil.toLong(resourceString.substring(1));
            } else throw new IllegalStateException("Can not identified string to resource: "+resourceString);
        }
    }

    @Override
    public String toString() {
        return "{" + metal + ", " + crystal + ", " + deuterium +"}";
    }
}
