package org.enoch.snark.instance.model.to;

import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.model.types.FleetDirectionType;

import java.time.LocalDateTime;

public class EventFleet {
    public String countDown;
    public LocalDateTime arrivalTime;
    public Mission mission;
    public ColonyType originFleet;
    public String coordsOrigin;
    public String detailsFleet;
    public FleetDirectionType iconMovement;
    public ColonyType destFleet;
    public String destCoords;
    public String sendProbe;
    public String sendMail;
    public boolean isHostile = false;

    @Override
    public String toString() {
        return "EventFleet{" +
                " " + isHostile +
                ", '" + countDown + '\'' +
                ", '" + arrivalTime + '\'' +
                ", '" + mission + '\'' +
                ", '" + originFleet + '\'' +
                ", '" + coordsOrigin + '\'' +
                ", '" + detailsFleet + '\'' +
                ", '" + iconMovement + '\'' +
                ", '" + destFleet + '\'' +
                ", '" + destCoords + '\'' +
                ", '" + sendProbe + '\'' +
                ", '" + sendMail + '\'' +
                '}';
    }

    public boolean isFleetImpactOnColony() {
        if(Mission.LIFE_FORM.equals(mission)) return false;
        return FleetDirectionType.BACK == iconMovement || Mission.STATIONED.equals(mission) || Mission.TRANSPORT.equals(mission);
    }

    public Planet getEndingPlanet() {
        if(FleetDirectionType.THERE.equals(iconMovement) &&
                (Mission.STATIONED.equals(mission) || (Mission.TRANSPORT.equals(mission)))) {
            return new Planet(destCoords, destFleet);
        }
        return new Planet(coordsOrigin, originFleet);
    }

    public Planet getFrom() {
        return new Planet(coordsOrigin, originFleet);
    }

    public Planet getTo() {
        return new Planet(destCoords, destFleet);
    }


}
