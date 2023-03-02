package org.enoch.snark.instance.commander;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class Navigator {

    private static Navigator INSTANCE;
    private List<EventFleet> eventFleetList;
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

    public boolean isExpiredForMinutes(int minutes) {
        return lastUpdate.plusMinutes(minutes).isBefore(LocalDateTime.now());
    }

    public void informAboutEventFleets(List<EventFleet> eventFleetList) {
        if(eventFleetList == null) {
            return;
        }
        this.eventFleetList = eventFleetList;
        this.lastUpdate = LocalDateTime.now();
    }

    public List<EventFleet> getEventFleetList() {
        return this.eventFleetList;
    }

    public boolean noneMission(Mission mission) {
        return this.eventFleetList.stream()
                .noneMatch(fleet -> mission.equals(fleet.mission));
    }

    public boolean isSimilarFleet(FleetEntity fleetEntity) {
        return this.eventFleetList.stream()
                .anyMatch(fleet ->
                        fleetEntity.source.toPlanet().equals(fleet.getFrom()) &&
                        fleetEntity.getTarget().equals(fleet.getTo()) &&
                                fleetEntity.mission.equals(fleet.mission));
    }

    public List<ColonyEntity> getSafeColonies(ColonyType type) {
        List<Planet> attackedPlanets = eventFleetList.stream()
                .filter(event -> event.isHostile)
                .map(eventFleet -> eventFleet.getTo())
                .collect(Collectors.toList());

        return ColonyDAO.getInstance().fetchAll().stream()
                .filter(colonyEntity -> colonyEntity.isPlanet && ColonyType.PLANET.equals(type))
                .filter(colonyEntity -> !attackedPlanets.contains(colonyEntity.toPlanet()))
                .collect(Collectors.toList());

    }

    @Override
    public String toString() {
        return eventFleetList.stream().map(event -> event + "\n")
                .collect(Collectors.joining("", "Navigator :\n", ""));
    }
}
