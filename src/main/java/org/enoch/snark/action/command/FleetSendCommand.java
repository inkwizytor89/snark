package org.enoch.snark.action.command;

import lombok.Data;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.util.ArrayList;
import java.util.List;

@Data
public class FleetSendCommand extends AbstractCommand implements SendCommand {
    private ColonyEntity source;
    private PlanetData target;
    private Mission mission;
    private Long speed;

    private ShipsMap shipsMap;
    private Resources resources;
    private final List<AbstractCondition> conditions = new ArrayList<>();
    private ShipsMap leaveShipsMap;
    private Resources leaveResources;

    private FleetPlan fleetPlan;

    public void addConditions(List<AbstractCondition> conditions) {
        if(conditions == null)  return;
        this.conditions.addAll(conditions);
    }

    public void generateHash(String hashPrefix, String code) {
        String prefix = hashPrefix != null ? hashPrefix+"_" : "";
        hash(prefix+ source+"_"+ mission+"_"+ target.getPlanet()+"_"+code);
    }

    public FleetContext createFleetContext() {
        return FleetContext.builder()
                .source(new PlanetData(source))
                .target(target)
                .leaveShipsMap(leaveShipsMap)
                .trip(fleetPlan.getTrip())
                .build();
    }

    @Override
    public String toString(){
        return hash();
    }
}
