package org.enoch.snark.instance.model.action.promisecondition;

import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;

import java.util.List;
import java.util.Map;
@Deprecated
public abstract class AbstractPromiseCondition {

    protected static final String MISSING_REASON = "Missing reason";

    public abstract boolean fit(FleetPromise colony);

    public abstract String reason(FleetPromise colony);

    private static AbstractPromiseCondition create(String key, String value) {
        String conditionTypeString = key.substring(key.indexOf("_") + 1).toUpperCase();
        ConditionType conditionType = ConditionType.valueOf(conditionTypeString);
        switch (conditionType) {
            case RESOURCE_IN_SOURCE: return new ResourcePromiseCondition(Resources.parse(value));
            case RESOURCES_COUNT_IN_SOURCE, RESOURCES_COUNT_IN_TARGET: return new ResourceCountPromiseCondition(NumberUtil.toLong(value), conditionType);
            case SPY_IS_NOT_EXPIRED: return new SpyNotExpiredPromiseCondition(NumberUtil.toLong(value),false);
            case SPY_IS_EXPIRED: return new SpyNotExpiredPromiseCondition(NumberUtil.toLong(value),true);
            case ATTACK_IS_NOT_EXPIRED: return new AttackNotExpiredPromiseCondition(NumberUtil.toLong(value),false);
            case ATTACK_IS_EXPIRED: return new AttackNotExpiredPromiseCondition(NumberUtil.toLong(value),true);
            case SHIPS_IN_SOURCE: return new ShipsPromiseCondition(ShipsMap.parse(value));
            case BLOCKING_MISSIONS: return new NoMissionsPromiseCondition(Mission.parse(value));
            case EXPEDITION: return new ExpeditionPromiseCondition(Boolean.parseBoolean(value));
        }
        throw new IllegalStateException("Can not map "+key+" to "+ConditionType.class.getName());
    }

    public static List<AbstractPromiseCondition> create(List<Map.Entry<String, String>> conditionsEntry) {
        return conditionsEntry.stream()
                .map(entry -> create(entry.getKey(), entry.getValue()))
                .toList();
    }
}
