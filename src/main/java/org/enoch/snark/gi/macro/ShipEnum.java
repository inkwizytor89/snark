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
        if(fleet.fighterLight != null && fleet.fighterLight > 0) shipsMap.put(fighterLight, fleet.fighterLight);
        if(fleet.fighterHeavy != null && fleet.fighterHeavy > 0) shipsMap.put(fighterHeavy, fleet.fighterHeavy);
        if(fleet.cruiser != null && fleet.cruiser > 0) shipsMap.put(cruiser, fleet.cruiser);
        if(fleet.battleship != null && fleet.battleship > 0) shipsMap.put(battleship, fleet.battleship);
        if(fleet.interceptor != null && fleet.interceptor > 0) shipsMap.put(interceptor, fleet.interceptor);
        if(fleet.bomber != null && fleet.bomber > 0) shipsMap.put(bomber, fleet.bomber);
        if(fleet.destroyer != null && fleet.destroyer > 0) shipsMap.put(destroyer, fleet.destroyer);
        if(fleet.deathstar != null && fleet.deathstar > 0) shipsMap.put(deathstar, fleet.deathstar);
        if(fleet.reaper != null && fleet.reaper > 0) shipsMap.put(reaper, fleet.reaper);
        if(fleet.explorer != null && fleet.explorer > 0) shipsMap.put(explorer, fleet.explorer);

        if(fleet.transporterSmall != null && fleet.transporterSmall > 0) shipsMap.put(transporterSmall, fleet.transporterSmall);
        if(fleet.transporterLarge != null && fleet.transporterLarge > 0) shipsMap.put(transporterLarge, fleet.transporterLarge);
        if(fleet.colonyShip != null && fleet.colonyShip > 0) shipsMap.put(colonyShip, fleet.colonyShip);
        if(fleet.recycler != null && fleet.recycler > 0) shipsMap.put(recycler, fleet.recycler);
        if(fleet.espionageProbe != null && fleet.espionageProbe > 0) shipsMap.put(espionageProbe, fleet.espionageProbe);
        return shipsMap;
    }

    public static Map<ShipEnum, Long> createExpeditionMap(Long tl, Long ts, Long ex) {
        Map<ShipEnum, Long> shipsMap = new HashMap<>();
        shipsMap.put(ShipEnum.transporterLarge, tl);
        shipsMap.put(ShipEnum.transporterSmall, ts);
        shipsMap.put(ShipEnum.explorer, ex);
        return shipsMap;
    }
}
