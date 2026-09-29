package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.Planet;

@Getter
public class ResourceCountCondition extends AbstractCondition {
    private final String resourcesCount;
    private final Planet planet;

    @JsonCreator
    public ResourceCountCondition(
            @JsonProperty("resourcesCount") String resourcesCount,
            @JsonProperty("planet") Planet planet
    ) {
        this.resourcesCount = resourcesCount;
        this.planet = planet;
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        PlanetEntity target = context.planet(planet);
        boolean isPossible = target.getResources().isCountMoreThan(resourcesCount);
        return isPossible ? null : this;
    }
}
