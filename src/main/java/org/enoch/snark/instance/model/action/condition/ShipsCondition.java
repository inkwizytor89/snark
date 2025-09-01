package org.enoch.snark.instance.model.action.condition;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.instance.model.action.promisecondition.AbstractPromiseCondition;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.uc.ShipUC;

@RequiredArgsConstructor
@Getter
public class ShipsCondition extends AbstractCondition {
    private final ShipsMap shipsMap;
    private final ShipsMap leave;
    private final Planet source;
}
