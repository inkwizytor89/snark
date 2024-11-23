package org.enoch.snark.instance.model.uc;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.SendFleetPromiseCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.model.action.condition.ResourceCondition;
import org.enoch.snark.instance.model.to.Resources;

import static org.enoch.snark.instance.model.uc.ResourceUC.isNothingOrNull;
import static org.enoch.snark.instance.model.uc.ResourceUC.sum;

public class FleetUC {

    public static SendFleetPromiseCommand transportFleet(ColonyEntity from, ColonyEntity to, Resources resources, Resources leave) {
        if (isNothingOrNull(resources)) return null;
        Resources needed = ResourceUC.toTransport(from, resources, leave);
        if (isNothingOrNull(needed)) return null;
//        Resources resourcesCondition = sum(needed, leave);
        return new FleetBuilder()
                .from(from)
                .to(to.toString())
                .mission(Mission.TRANSPORT)
//                .addCondition(new ResourceCondition(resourcesCondition))
                .addCondition(new ResourceCondition(needed))
                .leaveResources(leave)
                .resources(needed)
                .buildOne();
    }
}
