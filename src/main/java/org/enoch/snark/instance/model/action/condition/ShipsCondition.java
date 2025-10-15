package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.uc.ShipUC;

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
        PlanetEntity colonyEntity = context.planet(planet);
//        throw new NotImplementedException("ShipUC.fromExpressionToValues not implemented without promise");
        ShipsMap valuedMap = ShipUC.fromExpressionToValues(shipsMap, colonyEntity, leave);
        boolean isPossible = colonyEntity.hasEnoughShips(valuedMap);
        return isPossible ? null : this;
    }
}
