package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.service.Navigator;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NoMissionsCondition extends AbstractCondition {

    private final List<Mission> blockingMissions;
    private List<EventFleet> blockedFleets = new ArrayList<>();

    public NoMissionsCondition(List<Mission> missions) {
        this.blockingMissions = missions;
    }

    @Override
    public boolean fit(ColonyEntity colony) {
        if(blockingMissions == null) return true;
        blockedFleets = Navigator.getInstance().getEventFleetList().stream()
                .filter(fleet -> inAny(fleet.mission))
                .filter(fleet -> colony.toPlanet().equals(fleet.getEndingPlanet()))
                .toList();
        return blockedFleets.isEmpty();
    }

    private boolean inAny(Mission mission) {
        if(blockingMissions == null) return true;
        return blockingMissions.stream().anyMatch(blockingMission -> blockingMission.equals(mission));
    }

    @Override
    public String reason(ColonyEntity colony) {
        if(!fit(colony)) return colony + " is blocked by " + blockedFleets.stream().map(EventFleet::toString).collect(Collectors.joining(", "));
        else return MISSING_REASON;
    }
}
