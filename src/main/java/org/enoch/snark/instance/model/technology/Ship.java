package org.enoch.snark.instance.model.technology;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.types.UrlComponent;
import org.enoch.snark.instance.model.to.ShipsMap;

@Getter
@AllArgsConstructor
public enum Ship implements Technology {
    fighterLight(204L),
    fighterHeavy(205L),
    cruiser(206L),
    battleship(207L),
    interceptor(215L),
    bomber(211L),
    destroyer(213L),
    deathstar(214L),
    reaper(218L),
    explorer(219L),
    transporterSmall(202L),
    transporterLarge(203L),
    colonyShip(208L),
    recycler(209L),
    espionageProbe(210L),
    solarSatellite(212L);

    private Long id;

    public static ShipsMap createShipsMap(FleetEntity fleet) {
        ShipsMap shipsMap = new ShipsMap();
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

    public static ShipsMap createExpeditionShipMap(Long tl, Long ts, Long ex) {
        ShipsMap shipsMap = new ShipsMap();
        shipsMap.put(Ship.transporterLarge, tl);
        shipsMap.put(Ship.transporterSmall, ts);
        shipsMap.put(Ship.explorer, ex);
        return shipsMap;
    }

    @Override
    public UrlComponent getPage() {
        return null;
    }
}
