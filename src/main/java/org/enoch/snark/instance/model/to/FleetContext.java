package org.enoch.snark.instance.model.to;

import lombok.Builder;
import lombok.Getter;
import org.enoch.snark.expression.coordinate.ExpressionContext;

import java.util.List;

@Builder
@Getter
public class FleetContext implements ExpressionContext {
    List<PlanetData> trip;
    PlanetData source;
    PlanetData target;

    ShipsMap leaveShipsMap;

    @Override
    public String getSourceString() {
        return source.getPlanet().toString();
    }
//
//    public FleetContext(FleetPlan fleetPlan) {
//        trip = fleetPlan.getTrip();
//        source = new PlanetData(fleetPlan.getSource());
//    }
}
