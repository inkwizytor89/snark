package org.enoch.snark.instance.model.to;

import lombok.Builder;
import lombok.Data;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@Builder(toBuilder = true)
public class FleetPlan {
    private String source;
    private String target;
    private Mission mission;
    private Long speed;

    private List<ShipsMap> shipsWaves;
    private Resources resources = Resources.nothing;
    private List<AbstractCondition> conditions = new ArrayList<>();
    private List<ShipsMap> leaveShips;
    private Resources leaveResources;

    private List<PlanetData> trip;

    public static class FleetPlanBuilder {
        public FleetPlanBuilder ships(ShipsMap shipsMap) {
            shipsWaves = Collections.singletonList(shipsMap);
            return this;
        }
    }
}
