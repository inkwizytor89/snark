package org.enoch.snark.instance.commander;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.types.MissionType;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Navigator {

    private static Navigator INSTANCE;
    private List<EventFleet> eventFleetList = new ArrayList<>();
    private LocalDateTime lastUpdate = LocalDateTime.now().minusDays(1L);

    private Navigator() {
    }

    public static Navigator getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Navigator();
        }
        return INSTANCE;
    }

    public LocalDateTime getLastUpdate() {
        return lastUpdate;
    }

    public void informAboutEventFleets(List<EventFleet> eventFleetList) {
        if(eventFleetList == null) {
            System.err.println("Navigator loaded null data - skipping");
            return;
        }
        this.eventFleetList = eventFleetList;
        this.lastUpdate = LocalDateTime.now();
    }

    public List<EventFleet> getEventFleetList() {
        return this.eventFleetList;
    }

    public boolean noneMission(MissionType missionType) {
        return this.eventFleetList.stream()
                .noneMatch(fleet -> missionType.equals(fleet.missionType));
    }

    public boolean isSimilarFleet(FleetEntity fleetEntity) {
        return this.eventFleetList.stream()
                .anyMatch(fleet ->
                        fleetEntity.source.toPlanet().equals(fleet.getFrom()) &&
                        fleetEntity.getTarget().equals(fleet.getTo()) &&
                                fleetEntity.type.equals(fleet.missionType.getName()));
    }

    @Override
    public String toString() {
        return eventFleetList.stream().map(event -> event + "\n")
                .collect(Collectors.joining("", "Navigator :\n", ""));
    }
}
