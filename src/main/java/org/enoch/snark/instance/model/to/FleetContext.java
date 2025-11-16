package org.enoch.snark.instance.model.to;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FleetContext {
    List<PlanetData> trip;
    PlanetData source;
    PlanetData target;

    ShipsMap leaveShipsMap;
//
//    public FleetContext(FleetPlan fleetPlan) {
//        trip = fleetPlan.getTrip();
//        source = new PlanetData(fleetPlan.getSource());
//    }
}
