package org.enoch.snark.instance.model.to;

import org.apache.commons.lang3.EnumUtils;
import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.gi.types.ShipEnum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.types.Expression.ALL;
import static org.enoch.snark.instance.model.types.Expression.NONE;

public class ShipsMap extends HashMap<ShipEnum, Long> {

    public static final ShipsMap NO_SHIPS = new ShipsMap();
    public static final ShipsMap ALL_SHIPS = new ShipsMap();
    public static final List<ShipsMap> EMPTY_SHIP_WAVE = new ArrayList<>();

    public static ShipsMap parse(String expression) {
        if(ALL.is(expression)) return ALL_SHIPS;
        if(NONE.is(expression)) return NO_SHIPS;

        ShipsMap shipsMap = new ShipsMap();
        for (String position : expression.trim().split(",")) {
            String[] entry = position.split(":");
            ShipEnum shipEnumValue = EnumUtils.getEnum(ShipEnum.class, entry[0]);
            shipsMap.put(shipEnumValue, NumberUtil.toLong(entry[1]));
        }
        return shipsMap;
    }

    public static ShipsMap createSingle(ShipEnum key, Long value) {
        ShipsMap shipsMap = new ShipsMap();
        shipsMap.put(key, value);
        return shipsMap;
    }

//    public boolean isEmpty() {
//        return this.values().stream()
//                .anyMatch(value ->value > 0L);
//    }

    public ShipsMap leave(ShipsMap leaveMap) {
        if(ALL_SHIPS.equals(this) || NO_SHIPS.equals(this))
            throw new IllegalStateException("Expression maps are abstract - leave");

        ShipsMap result = new ShipsMap();
        if(ALL_SHIPS.equals(leaveMap)) return result;
        else if(leaveMap == null || NO_SHIPS.equals(leaveMap)) result.putAll(this);
        else this.forEach((baseKey, baseValue) -> {
            Long leaveValue = leaveMap.get(baseKey);
            if (leaveValue == null) result.put(baseKey, baseValue);
            else {
                long newValue = baseValue - leaveValue;
                if(newValue > 0) result.put(baseKey, newValue);
            }
        });
        return result;
    }

    public ShipsMap reduce(ShipsMap reduceMap) {
        if(ALL_SHIPS.equals(this) || NO_SHIPS.equals(this))
            throw new IllegalStateException("Expression maps are abstract - reduce");

        ShipsMap result = new ShipsMap();
        if(ALL_SHIPS.equals(reduceMap)) result.putAll(this);
        if(reduceMap == null || NO_SHIPS.equals(reduceMap))  return result;

        this.forEach((baseKey, baseValue) -> {
            Long reduceValue = reduceMap.get(baseKey);
            if (reduceValue != null) result.put(baseKey, Math.min(baseValue, reduceValue));
        });
        return result;
    }

    public Long count() {
        return values().stream().mapToLong(value -> value).sum();
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj);
    }

    @Override
    public String toString() {
        if(ALL_SHIPS.equals(this)) return "{ALL_SHIPS}";
        if(NO_SHIPS.equals(this)) return "{NO_SHIPS}";
        return "{"+ this.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .collect(Collectors.joining(", ")) +
                "}";
    }
}
