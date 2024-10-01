package org.enoch.snark.instance.model.to;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.types.FleetDirectionType;

import java.time.LocalDateTime;

@Builder(toBuilder=true)
@Getter
@EqualsAndHashCode
public class FleetMovement {

    boolean temporary;
    boolean hostile;
    Planet from;
    Planet to;
    LocalDateTime arrivalTime;
    Mission mission;
    FleetDirectionType direction;
    Long count;

    public boolean haveImpactOnColony() {
        if(Mission.LIFE_FORM.equals(mission)) return false;
        if(Mission.SPY.equals(mission)) return false;
        return FleetDirectionType.BACK == direction || Mission.STATIONED.equals(mission) || Mission.TRANSPORT.equals(mission);
    }

    @Override
    public String toString() {
        return "FleetMovement{" +
                "temporary=" + temporary +
                ", from=" + from +
                ", to=" + to +
                ", arrivalTime=" + arrivalTime +
                ", mission=" + mission +
                ", direction=" + direction +
                '}';
    }
}
