package org.enoch.snark.instance.service;

import lombok.Getter;
import lombok.Setter;
import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.action.command.OpenPageCommand;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.FleetMovement;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.FleetDirectionType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.si.module.consumer.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.model.types.FleetDirectionType.THERE;

public class Navigator {

    public static final long TIME_DELAY_IN_SECONDS = 4L;

    private static Navigator INSTANCE;

    @Setter
    private static int fleetCount = 0;
    @Setter
    private static int fleetMax = 1;
    @Setter
    private static int expeditionCount = 0;
    @Getter
    @Setter
    private static int expeditionMax = 0;

    private List<EventFleet> eventFleetList;
    private LocalDateTime lastUpdate = LocalDateTime.now().minusDays(1L);
    private final Set<FleetMovement> movements = new HashSet<>();
    private static final Object movementsLock = new Object();

    private Navigator() {
//        start();
    }

    public static Navigator getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Navigator();
        }
        return INSTANCE;
    }

    public static boolean isFleetFreeSlot() {
        return getFleetFreeSlots() > 0;
    }

    public static int getFleetFreeSlots() {
        return fleetMax - fleetCount;
    }

    public static boolean isExpeditionFreeSlot() {
        return getFleetFreeSlots() > 0;
    }

    public static int getExpeditionFreeSlots() {
        return expeditionMax - expeditionCount;
    }

    public List<FleetMovement> pollExpired() {
        LocalDateTime relativeNow = LocalDateTime.now().minusSeconds(TIME_DELAY_IN_SECONDS);
        List<FleetMovement> expiredMovements = movements.stream()
                .filter(movement -> relativeNow.isAfter(movement.getArrivalTime()))
                .toList();
        expiredMovements.forEach(movements::remove);
        return expiredMovements;
    }

    public boolean isExpiredAfter(Duration duration) {
        return lastUpdate.plusSeconds(duration.getSeconds()).isBefore(LocalDateTime.now());
    }

    public void informAboutEventFleets(List<EventFleet> eventFleetList) {
        if(eventFleetList == null) {
            System.err.println("Try to push null eventFleetList - skipping");
            return;
        }
        this.eventFleetList = eventFleetList;
        this.lastUpdate = LocalDateTime.now();
        eventFleetList.forEach(this::add);
    }

    @Deprecated
    public List<EventFleet> getEventFleetList() {
        return this.eventFleetList;
    }

    public void add(EventFleet fleet) {
        add(FleetMovement.builder()
                .hostile(fleet.isHostile)
                .from(fleet.getFrom())
                .to(fleet.getTo())
                .arrivalTime(fleet.arrivalTime)
                .mission(fleet.mission)
                .direction(fleet.iconMovement)
                .count(Long.parseLong(fleet.detailsFleet))
                .build());
    }

    public void add(FleetEntity fleet) {
        Planet sourcePlanet = fleet.source.toPlanet();
        Planet targetPlanet = fleet.getTarget();
        Long shipsCount = fleet.getShips().count();
        FleetMovement movementThere = FleetMovement.builder()
                .temporary(true)
                .from(sourcePlanet)
                .to(targetPlanet)
                .arrivalTime(fleet.visited.plusSeconds(8))
                .mission(fleet.mission)
                .direction(THERE)
                .count(shipsCount)
                .build();
        add(movementThere);
        if(fleet.mission.isComingBack()) {
            FleetMovement movementBack = movementThere.toBuilder()
                    .from(sourcePlanet)
                    .to(targetPlanet)
                    .arrivalTime(fleet.back.plusSeconds(8))
                    .direction(FleetDirectionType.BACK)
                    .count(shipsCount)
                    .build();
            add(movementBack);
        }
    }

    private void add(FleetMovement movement) {
        movements.add(movement);
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
