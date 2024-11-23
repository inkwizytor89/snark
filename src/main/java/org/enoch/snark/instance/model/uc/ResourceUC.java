package org.enoch.snark.instance.model.uc;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.types.ColonyType;

import static org.enoch.snark.instance.model.to.Resources.everything;
import static org.enoch.snark.instance.model.to.Resources.nothing;

public class ResourceUC {


    public static Resources toTransport(FleetPromise promise) {
        return toTransport(promise.getSource(), promise.getResources(), promise.getLeaveResources());
    }

    public static Resources toTransport(ColonyEntity colony, Resources requested, Resources leave) {
        return toTransport(colony.getResources(), requested, leave, colony.type);
    }

    public static Resources toTransport(Resources current, Resources requested, Resources leave, ColonyType colonyType) {
        if(leave == null) leave = Instance.getGlobalMainConfigMap().getNearestLeaveResources(colonyType, nothing);
        if(everything.equals(requested) && nothing.equals(leave)) return everything;
        if(everything.equals(requested) && !nothing.equals(leave)) return current.missing(leave);
        Resources needed = requested.plus(leave);
        // ten warunek jest zbedny bo redukuje sie do baseDeficit 0 i needDeficit 0
        if(current.isEnough(needed)) return requested;
        // colony is in deficit before the transfer of resources, if we transfer them the deficit will not increase
        if(isDeficitNotIncrease(current, needed, leave)) return requested;
        // we do not allow the deficit to increase
        return null;
    }

    private static boolean isDeficitNotIncrease(Resources current, Resources needed, Resources leave) {
        Resources baseDeficit = current.deficit(leave);
        Resources needDeficit = current.deficit(needed);
        return baseDeficit.isEnough(needDeficit);
    }

    public static boolean isNothingOrNull(Resources resources) {
        return resources == null || nothing.equals(resources);
    }

    public static boolean isEverything(Resources resources) {
        return everything.equals(resources);
    }

    public static Resources sum(Resources a, Resources b) {
        if(isEverything(a) || isEverything(b)) return everything;
        if(a == null) a = nothing;
        if(b == null) b = nothing;
        // fix overlap long.MAX
        Resources result = new Resources();
        result.metal = a.metal + b.metal;
        result.crystal = a.crystal + b.crystal;
        result.deuterium = a.deuterium + b.deuterium;
        return result;
    }
}
