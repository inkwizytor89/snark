package org.enoch.snark.instance.model.uc;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;

import static org.enoch.snark.instance.model.to.Resources.everything;
import static org.enoch.snark.instance.model.to.Resources.nothing;

public class ResourceUC {

    public static Resources calculate(Resources current, Resources requested, ColonyType colonyType) {
        return calculate(current, requested, null, colonyType);
    }

    public static Resources calculate(ColonyEntity colony, Resources requested, Resources leave) {
        return calculate(colony.getResources(), requested, leave, colony.type);
    }

    public static Resources calculate(Resources current, Resources requested, Resources leave, ColonyType colonyType) {
        if(leave == null) leave = Instance.getGlobalMainConfigMap().getNearestLeaveResources(colonyType, nothing);
        if(everything.equals(requested) && nothing.equals(leave)) return everything;
        if(everything.equals(requested) && !nothing.equals(leave)) return current.missing(leave);
        Resources needed = requested.plus(leave);
        if(current.isEnough(needed)) return requested;
        return null;
    }
}
