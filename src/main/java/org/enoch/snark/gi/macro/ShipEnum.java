package org.enoch.snark.gi.macro;

import org.enoch.snark.db.entity.FleetEntity;

import java.util.HashMap;
import java.util.Map;

public enum ShipEnum {
    LM("id=ship_204"),
    CM("id=ship_205"),
    KR("id=ship_206"),
    OW("id=ship_207"),
    PAN("id=ship_215"),
    BOM("id=ship_211"),
    NI("id=ship_213"),
    GS("id=ship_214 "),
    LT("id=ship_202"),
    DT("id=ship_203"),
    KOL("id=ship_208"),
    REC("id=ship_209"),
    SON("id=ship_210");

    private String id;

    ShipEnum(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static Map<ShipEnum, Long> createShipsMap(FleetEntity fleet) {
        Map<ShipEnum, Long> shipsMap = new HashMap<>();
        if(fleet.lm != null && fleet.lm > 0) shipsMap.put(LM, fleet.lm);
        if(fleet.cm != null && fleet.cm > 0) shipsMap.put(CM, fleet.cm);
        if(fleet.kr != null && fleet.kr > 0) shipsMap.put(KR, fleet.kr);
        if(fleet.ow != null && fleet.ow > 0) shipsMap.put(OW, fleet.ow);
        if(fleet.pan != null && fleet.pan > 0) shipsMap.put(PAN, fleet.pan);
        if(fleet.bom != null && fleet.bom > 0) shipsMap.put(BOM, fleet.bom);
        if(fleet.ni != null && fleet.ni > 0) shipsMap.put(NI, fleet.ni);
        if(fleet.gs != null && fleet.gs > 0) shipsMap.put(GS, fleet.gs);
        if(fleet.lt != null && fleet.lt > 0) shipsMap.put(LT, fleet.lt);
        if(fleet.dt != null && fleet.dt > 0) shipsMap.put(DT, fleet.dt);
        if(fleet.kol != null && fleet.kol > 0) shipsMap.put(KOL, fleet.kol);
        if(fleet.rec != null && fleet.rec > 0) shipsMap.put(REC, fleet.rec);
        if(fleet.son != null && fleet.son > 0) shipsMap.put(SON, fleet.son);
        return shipsMap;
    }
}
