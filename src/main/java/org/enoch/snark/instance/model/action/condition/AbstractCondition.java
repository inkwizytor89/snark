package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

import java.util.List;
import java.util.Map;

public abstract class AbstractCondition {

    protected static final String MISSING_REASON = "Missing reason";

    public abstract boolean fit(FleetPromise colony);

    public abstract String reason(FleetPromise colony);

    private static AbstractCondition create(String key, String value) {
        String conditionTypeString = key.substring(key.indexOf("_") + 1).toUpperCase();
        ConditionType conditionType = ConditionType.valueOf(conditionTypeString);
        switch (conditionType) {
            case RESOURCE_IN_SOURCE: return new ResourceCondition(Resources.parse(value));
            case RESOURCES_COUNT_IN_SOURCE, RESOURCES_COUNT_IN_TARGET: return new ResourceCountCondition(NumberUtil.toLong(value), conditionType);
            case SPY_NOT_EXPIRED: return new SpyNotExpiredCondition(NumberUtil.toLong(value));
            case SHIPS_IN_SOURCE: return new ShipsCondition(ShipsMap.parse(value));
            case BLOCKING_MISSIONS: return new NoMissionsCondition(Mission.parse(value));
        }
        throw new IllegalStateException("Can not map "+key+" to "+ConditionType.class.getName());
    }

    public static List<AbstractCondition> create(List<Map.Entry<String, String>> conditionsEntry) {
        return conditionsEntry.stream()
                .map(entry -> create(entry.getKey(), entry.getValue()))
                .toList();
    }
}
