package org.enoch.snark.instance.model.to;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.instance.model.types.ResourceType;

import java.util.Arrays;

import static org.enoch.snark.instance.model.types.ResourceType.*;

public class Resources {

    public static Resources unknown = new Resources(-1L, -1L, -1L);
    public static Resources everything = new Resources(-100L, -100L, -100L);
    public static Resources nothing = new Resources(0L, 0L, 0L);

    public Long metal = 0L;
    public Long crystal = 0L;
    public Long deuterium = 0L;

    public boolean skipLeaveMetal;
    public boolean skipLeaveCrystal;
    public boolean skipLeaveDeuterium;

    public static Resources parse(String resourceExpression) {
        if (StringUtils.isEmpty(resourceExpression) ||
                resourceExpression.contains("nothing") ||
                resourceExpression.contains("none"))
            return nothing;
        if (resourceExpression.contains("all") ||
                resourceExpression.contains("everything"))
            return everything;
        return new Resources(resourceExpression);
    }

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

    public Long count() {
        return metal + crystal + deuterium;
    }

    @Override
    public String toString() {
        return "{" + metal + ", " + crystal + ", " + deuterium +"}";
    }

    public boolean isCountMoreThan(String s) {
        return isCountMoreThan(NumberUtil.toLong(s));
    }

    public boolean isCountMoreThan(Long value) {
        if(value == null) return true;
        return count() >= value;
    }

    public boolean isMoreThan(Resources resources) {
        if(resources == null || everything.equals(resources) || nothing.equals(resources)) return true;
        return metal >= resources.metal &&
                crystal >= resources.crystal &&
                deuterium >= resources.deuterium;
    }

    public Resources skipLeave(ResourceType... types) {
        Arrays.stream(types).forEach(toSkip -> {
            if(METAL.equals(toSkip)) skipLeaveMetal = true;
            else if (CRYSTAL.equals(toSkip)) skipLeaveCrystal = true;
            else if (DEUTERIUM.equals(toSkip)) skipLeaveDeuterium = true;
        });
        return this;
    }
}
