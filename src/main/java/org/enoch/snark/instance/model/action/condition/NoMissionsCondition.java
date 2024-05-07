package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.service.Navigator;

import java.util.List;

public class NoMissionsCondition extends AbstractCondition {

    private final List<Mission> blockingMissions;

    public NoMissionsCondition(List<Mission> missions) {
        this.blockingMissions = missions;
    }

    @Override
    public boolean fit(ColonyEntity colony) {
        if(blockingMissions == null) return true;
        return Navigator.getInstance().getEventFleetList().stream()
                .filter(fleet -> inAny(fleet.mission))
                .noneMatch(fleet -> colony.toPlanet().equals(fleet.getEndingPlanet()));
    }

    private boolean inAny(Mission mission) {
        if(blockingMissions == null) return true;
        return blockingMissions.stream().anyMatch(blockingMission -> blockingMission.equals(mission));
    }
}
