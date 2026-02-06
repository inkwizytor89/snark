package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.FleetContext;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.PlanetData;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.service.ShipService;

@Getter
public class ShipsCondition extends AbstractCondition {
    private final ShipsMap shipsMap;
    private final ShipsMap leave;
    private final Planet planet;

    @JsonCreator
    public ShipsCondition(
            @JsonProperty("shipsMap") ShipsMap shipsMap,
            @JsonProperty("leave") ShipsMap leave,
            @JsonProperty("planet") Planet planet
    ) {
        this.shipsMap = shipsMap;
        this.leave = leave;
        this.planet = planet;
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        PlanetData planetData = context.planetData(planet);
        ShipsMap valuedMap = context.getShipService().fromExpressionToValues(shipsMap);
        boolean isPossible = planetData.planetData().hasEnoughShips(valuedMap);
        return isPossible ? null : this;
    }
}
