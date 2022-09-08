package org.enoch.snark.gi.macro;

import org.enoch.snark.db.entity.FleetEntity;

import java.util.HashMap;
import java.util.Map;

public enum ShipEnum {
    fighterLight("fighterLight"),
    fighterHeavy("fighterHeavy"),
    cruiser("cruiser"),
    battleship("battleship"),
    interceptor("interceptor"),
    bomber("bomber"),
    destroyer("destroyer"),
    deathstar("deathstar"),
    reaper("reaper"),
    explorer("explorer"),
    transporterSmall("transporterSmall"),
    transporterLarge("transporterLarge"),
    colonyShip("colonyShip"),
    recycler("recycler"),
    espionageProbe("espionageProbe");

    private String id;

    ShipEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Map<ShipEnum, Long> createShipsMap(FleetEntity fleet) {
        Map<ShipEnum, Long> shipsMap = new HashMap<>();
        if(fleet.lm != null && fleet.lm > 0) shipsMap.put(fighterLight, fleet.lm);
        if(fleet.cm != null && fleet.cm > 0) shipsMap.put(fighterHeavy, fleet.cm);
        if(fleet.kr != null && fleet.kr > 0) shipsMap.put(cruiser, fleet.kr);
        if(fleet.ow != null && fleet.ow > 0) shipsMap.put(battleship, fleet.ow);
        if(fleet.pan != null && fleet.pan > 0) shipsMap.put(interceptor, fleet.pan);
        if(fleet.bom != null && fleet.bom > 0) shipsMap.put(bomber, fleet.bom);
        if(fleet.ni != null && fleet.ni > 0) shipsMap.put(destroyer, fleet.ni);
        if(fleet.gs != null && fleet.gs > 0) shipsMap.put(deathstar, fleet.gs);
        if(fleet.lt != null && fleet.lt > 0) shipsMap.put(transporterSmall, fleet.lt);
        if(fleet.dt != null && fleet.dt > 0) shipsMap.put(transporterLarge, fleet.dt);
        if(fleet.kol != null && fleet.kol > 0) shipsMap.put(colonyShip, fleet.kol);
        if(fleet.rec != null && fleet.rec > 0) shipsMap.put(recycler, fleet.rec);
        if(fleet.son != null && fleet.son > 0) shipsMap.put(espionageProbe, fleet.son);
        if(fleet.pf != null && fleet.pf > 0) shipsMap.put(explorer, fleet.pf);
        if(fleet.re != null && fleet.re > 0) shipsMap.put(reaper, fleet.re);
        return shipsMap;
    }
}
