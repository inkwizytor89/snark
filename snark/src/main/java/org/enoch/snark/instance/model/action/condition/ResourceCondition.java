package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;

import static org.enoch.snark.instance.model.to.Resources.nothing;
import static org.enoch.snark.instance.model.uc.ResourceUC.toTransport;

@Getter
public class ResourceCondition extends AbstractCondition {
    private final Planet planet;
    private final Resources resources;
    private final Resources leaveResources;

    @JsonCreator
    public ResourceCondition(
            @JsonProperty("planet") Planet planet,
            @JsonProperty("resources") Resources resources,
            @JsonProperty("leaveResources") Resources leaveResources
    ) {
        this.planet = planet;
        this.resources = resources;
        this.leaveResources = leaveResources;
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        if(nothing.equals(resources)) return null;
        PlanetEntity planetEntity = context.planet(planet);
        boolean isPossible = toTransport(planetEntity, resources, leaveResources) != null;
        return isPossible ? null : this;
    }
}
