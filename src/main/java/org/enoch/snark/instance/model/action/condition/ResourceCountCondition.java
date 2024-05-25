package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.FleetPromise;

import java.util.Optional;

import static org.enoch.snark.instance.model.action.condition.ConditionType.RESOURCES_COUNT_IN_TARGET;

public class ResourceCountCondition extends AbstractCondition {
    private final Long resourcesCount;
    private final ConditionType conditionType;

    private PlanetEntity planetEntity;

    public ResourceCountCondition(Long resourcesCount, ConditionType conditionType) {
        this.resourcesCount = resourcesCount;
        this.conditionType = conditionType;
    }

    @Override
    public boolean fit(FleetPromise promise) {
        this.planetEntity = getPlanetEntity(promise);
        return this.planetEntity.getResources().isCountMoreThan(resourcesCount.toString());
    }

    @Override
    public String reason(FleetPromise promise) {
        if(!fit(promise)) return planetEntity + " have " + planetEntity.getResources() + " but count is needed " + resourcesCount;
        else return MISSING_REASON;
    }

    private PlanetEntity getPlanetEntity(FleetPromise promise) {
        if(RESOURCES_COUNT_IN_TARGET.equals(conditionType)) {
            Optional<TargetEntity> targetEntityOptional = TargetDAO.getInstance().find(promise.getTarget());
            if(targetEntityOptional.isPresent()) return targetEntityOptional.get();
            else throw new IllegalStateException(promise.getTarget() + " can not find in database");
        }
        return ColonyDAO.getInstance().get(promise.getSource().toPlanet());
    }
}
