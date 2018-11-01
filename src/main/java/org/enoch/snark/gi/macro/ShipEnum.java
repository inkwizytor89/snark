package org.enoch.snark.gi.macro;

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
}
