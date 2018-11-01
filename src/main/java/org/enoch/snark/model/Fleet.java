package org.enoch.snark.model;

import org.enoch.snark.gi.macro.ShipEnum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Fleet {

    private Map<ShipEnum, Integer> fleetMap = new HashMap<>();

    public Fleet put(ShipEnum shipEnum, Integer count) {
        fleetMap.put(shipEnum, count);
        return this;
    }

    public Set<Map.Entry<ShipEnum, Integer>> getEntry() {
        return fleetMap.entrySet();
    }
}
