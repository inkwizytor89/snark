package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.springframework.lang.Nullable;

import java.util.List;

import static org.enoch.snark.instance.si.module.consumer.gi.types.Mission.convertMissions;

@Getter
public class NoMissionsCondition extends AbstractCondition {

    private final Planet source;
    private final List<Mission> blockingMissions;
    private final Duration duration;

    @JsonCreator
    public NoMissionsCondition(
            @JsonProperty("source") Planet source,
            @JsonProperty("blockingMissions") String blockingMissions,
            @JsonProperty("duration") @Nullable String duration
    ) {
        this.source = source;
        this.blockingMissions = convertMissions(blockingMissions);
        this.duration = duration != null ? new Duration(duration) : new Duration("1H");
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        ColonyEntity colonyEntity = context.colony(source);
        if(getBlockingMissions() == null) return null;
        List<EventFleet> blockedFleets = Navigator.getInstance().getEventFleetList().stream()
                .filter(fleet -> inAny(fleet.mission, blockingMissions))
                .filter(fleet -> colonyEntity.toPlanet().equals(fleet.getEndingPlanet()))
                .filter(fleet -> !DateUtil.isExpired(fleet.arrivalTime, duration.getValue()))
                .toList();
        boolean isPossible = blockedFleets.isEmpty();
        return isPossible ? null : this;
    }

    private boolean inAny(Mission mission, List<Mission> blockingMissions) {
        if(blockingMissions == null) return true;
        return blockingMissions.stream().anyMatch(blockingMission -> blockingMission.equals(mission));
    }
}
