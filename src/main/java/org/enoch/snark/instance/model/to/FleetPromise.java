package org.enoch.snark.instance.model.to;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FleetPromise {
//    private ColonyEntity from;
//    private Planet to;
//    private Mission mission;
    private ShipsMap shipsMap;
    private Resources resources;
//    private Long speed;
//    private String code;


    public ShipsMap getShipsMap() {
        return shipsMap;
    }

    public void setShipsMap(ShipsMap shipsMap) {
        this.shipsMap = shipsMap;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }
}
