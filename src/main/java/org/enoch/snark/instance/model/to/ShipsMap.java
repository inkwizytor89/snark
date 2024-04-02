package org.enoch.snark.instance.model.to;

import org.enoch.snark.gi.types.ShipEnum;

import java.util.HashMap;

public class ShipsMap extends HashMap<ShipEnum, Long> {
    public static final ShipsMap EMPTY = new ShipsMap();

    public static ShipsMap create(String code) {
        return ShipEnum.parse(code);
    }
}
