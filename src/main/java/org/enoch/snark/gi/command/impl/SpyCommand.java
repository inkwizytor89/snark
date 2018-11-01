package org.enoch.snark.gi.command.impl;

import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Fleet;
import org.enoch.snark.model.Planet;

public class SpyCommand extends SendFleetCommand {

    public SpyCommand(Instance instance, Planet target) {
        this(instance, target, 1);
    }

    public SpyCommand(Instance instance, Planet target, Integer count) {
        super(instance, target, Mission.SPY, new Fleet().put(ShipEnum.SON, count));
    }

}
