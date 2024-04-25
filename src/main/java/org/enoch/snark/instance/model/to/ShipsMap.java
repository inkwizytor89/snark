package org.enoch.snark.instance.model.to;

import org.apache.commons.lang3.EnumUtils;
import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.model.types.Expression;

import java.util.HashMap;
import java.util.Map;

import static org.enoch.snark.instance.model.types.Expression.ALL;
import static org.enoch.snark.instance.model.types.Expression.NONE;

public class ShipsMap extends HashMap<ShipEnum, Long> {

    public static final ShipsMap NO_SHIPS = new ShipsMap();
    public static final ShipsMap ALL_SHIPS = new ShipsMap();

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
}
