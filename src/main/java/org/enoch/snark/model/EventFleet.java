package org.enoch.snark.model;

import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.model.types.FleetDirectionType;
import org.enoch.snark.model.types.MissionType;

import java.time.LocalDateTime;

public class EventFleet {
    public String countDown;
    public LocalDateTime arrivalTime;
    public MissionType missionType;
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
                ", '" + missionType + '\'' +
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
        return FleetDirectionType.BACK == iconMovement || MissionType.STATION.equals(missionType) || MissionType.TRANSPORT.equals(missionType);
    }

    public Planet getEndingPlanet() {
        if(FleetDirectionType.THERE.equals(iconMovement) &&
                (MissionType.STATION.equals(missionType) || (MissionType.TRANSPORT.equals(missionType)))) {
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
