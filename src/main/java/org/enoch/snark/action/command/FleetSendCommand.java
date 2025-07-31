package org.enoch.snark.action.command;

import lombok.Data;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.action.condition.AbstractCondition;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;

import java.util.ArrayList;
import java.util.List;

@Data
public class FleetSendCommand extends AbstractCommand {
    private ColonyEntity source;
    private Planet target;
    private Mission mission;
    private Long speed;

    private ShipsMap shipsMap;
    private Resources resources;
    private final List<AbstractCondition> conditions = new ArrayList<>();
    private ShipsMap leaveShipsMap;
    private Resources leaveResources;

    private FleetPromise promise;


    public void addConditions(List<AbstractCondition> conditions) {
        this.conditions.addAll(conditions);
    }

    public void generateHash(String hashPrefix, String code) {
        String prefix = hashPrefix != null ? hashPrefix+"_" : "";
        hash(prefix+promise.getSource()+"_"+promise.getMission()+"_"+promise.getTarget()+"_"+code);
    }
}
