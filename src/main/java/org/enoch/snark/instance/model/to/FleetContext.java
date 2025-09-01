package org.enoch.snark.instance.model.to;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class FleetContext {
    List<PlanetData> trip;
    PlanetData source;
//
//    public FleetContext(FleetPlan fleetPlan) {
//        trip = fleetPlan.getTrip();
//        source = new PlanetData(fleetPlan.getSource());
//    }


    public static FleetContext fromFleetPlan(FleetPlan fleetPlan) {
        return FleetContext.builder()
                .trip(fleetPlan.getTrip())
                .source(new PlanetData(fleetPlan.getSource()))
                .build();
    }
}
