package org.enoch.snark.instance.service;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.model.to.EventFleet;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Navigator {

    private static Navigator INSTANCE;
    private List<EventFleet> eventFleetList;
    private List<FleetEntity> quickFleets = new ArrayList<>();
    private LocalDateTime lastUpdate = LocalDateTime.now().minusDays(1L);

    private Navigator() {
    }

    public static Navigator getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Navigator();
        }
        return INSTANCE;
    }

    public boolean isExpiredAfterMinutes(int minutes) {
        return lastUpdate.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }

    public void informAboutEventFleets(List<EventFleet> eventFleetList) {
        int fleetCount = Commander.getInstance().getFleetCount();
        if(eventFleetList == null || eventFleetList.size() < fleetCount) {
            return;
        }
        this.eventFleetList = eventFleetList;
        this.quickFleets = new ArrayList<>();
        this.lastUpdate = LocalDateTime.now();
    }

    public void informAboutFleetEntity(FleetEntity fleetEntity) {
        quickFleets.add(fleetEntity);
    }

    public List<EventFleet> getEventFleetList() {
        return this.eventFleetList;
    }

    public List<FleetEntity> getQuickFleets() {
        return quickFleets;
    }

    public boolean noneMission(Mission mission) {
        return this.eventFleetList.stream()
                .noneMatch(fleet -> mission.equals(fleet.mission));
    }

    @Override
    public String toString() {
        return eventFleetList.stream().map(event -> event + "\n")
                .collect(Collectors.joining("", "Navigator :\n", ""));
    }
}
