package org.enoch.snark.instance.service;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.FleetMovement;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.FleetDirectionType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.model.types.FleetDirectionType.THERE;

public class Navigator {

    public static final long TIME_DELAY_IN_SECONDS = 4L;
    private static Navigator INSTANCE;
    private List<EventFleet> eventFleetList;
    private LocalDateTime lastUpdate = LocalDateTime.now().minusDays(1L);
    private final Set<FleetMovement> movements = new HashSet<>();
    private static final Object movementsLock = new Object();

    private Navigator() {
        start();
    }

    public static Navigator getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new Navigator();
        }
        return INSTANCE;
    }

    public void start() {
        Runnable task = () -> {
            while(true) try {
                SleepUtil.sleep();
                synchronized (movementsLock) {
                    List<FleetMovement> pulled = pollExpired();
//                    pulled.forEach(System.err::println);
                    updateColonies(pulled);
                }
            } catch (Throwable e) {
                throw new IllegalStateException(e);
            }
        };
        new Thread(task).start();
    }

    private List<FleetMovement> pollExpired() {
        LocalDateTime relativeNow = LocalDateTime.now().minusSeconds(TIME_DELAY_IN_SECONDS);
        List<FleetMovement> expiredMovements = movements.stream()
                .filter(movement -> relativeNow.isAfter(movement.getArrivalTime()))
                .toList();
        expiredMovements.forEach(movements::remove);
        return expiredMovements;
    }

    private void updateColonies(List<FleetMovement> movements) {
        movements.stream()
                .filter(FleetMovement::haveImpactOnColony)
                .forEach(movement -> {
            Planet toUpdate = THERE.equals(movement.getDirection()) ? movement.getTo() : movement.getFrom();
            ColonyEntity colony = ColonyDAO.getInstance().find(toUpdate);
            if(colony != null) {
                new OpenPageCommand(FLEETDISPATCH, colony).sourceHash(this.getClass().getSimpleName()).push();
            }
        });
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
        this.lastUpdate = LocalDateTime.now();
        removeAllTemporaryMovements();
        eventFleetList.forEach(this::add);

    }

    private void removeAllTemporaryMovements() {
            movements.stream()
                    .filter(FleetMovement::isTemporary)
                    .forEach(movements::remove);
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
