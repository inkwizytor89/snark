package org.enoch.snark.model;

import org.enoch.snark.gi.macro.ShipEnum;

public class AttackPlan {

    public final SourcePlanet source;
    public final Planet target;
    public final Fleet fleet;

    public AttackPlan(SpyInfo info) {
        source = info.source;
        target = info.planet;

        int lTCount = (int) ((info.getSumResourceCount()/ 5000) +3);

        fleet = new Fleet().put(ShipEnum.LT, lTCount);
    }
}
