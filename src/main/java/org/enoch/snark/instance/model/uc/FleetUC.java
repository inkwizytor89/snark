package org.enoch.snark.instance.model.uc;

import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.action.command.SendFleetPromiseCommand;
import org.enoch.snark.instance.model.to.Resources;

public class FleetUC {

    public static SendFleetPromiseCommand transportFleet(ColonyEntity from, ColonyEntity to, Resources resources, Resources leave) {
        throw new NotImplementedException("To fix in spring version");
//        if (isNothingOrNull(resources)) return null;
//        Resources needed = ResourceUC.toTransport(from, resources, leave);
//        if (isNothingOrNull(needed)) return null;
////        Resources resourcesCondition = sum(needed, leave);
//        return new FleetBuilder()
//                .from(from)
//                .to(to.toString())
//                .mission(Mission.TRANSPORT)
////                .addCondition(new ResourceCondition(resourcesCondition))
//                .addCondition(new ResourceCondition(needed))
//                .leaveResources(leave)
//                .resources(needed)
//                .buildOne();
    }
}
