package org.enoch.snark.instance.si.module.update;

import org.enoch.snark.common.NumberUtil;
import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.LoadColoniesCommand;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.UpdateFleetEventsCommand;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.commander.QueueRunType;
import org.enoch.snark.instance.model.action.find.ProbeSwarmFinder;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.types.UrlComponent.FLEETDISPATCH;
import static org.enoch.snark.instance.model.action.PlanetExpression.PROBE_SWAM;

public class UpdateThread extends AbstractThread {

    public static final String threadName = "update";
    public static final String REFRESH = "refresh";
    public int updateTimeInMinutes = 12;

    private Navigator navigator;
    private List<EventFleet> events;
    private final HashMap<LocalDateTime, Planet> arrivedMap = new HashMap<>();

    public UpdateThread(ConfigMap map) {
        super(map);
    }

    @Override
    protected void onStart() {
        super.onStart();
        navigator = Navigator.getInstance();
        pause = 10;
    }

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return pause;
    }

    @Override
    protected void onStep() {
        updateTimeInMinutes = map.getConfigInteger(REFRESH, 12);
        if(isNavigatorExpired() && commander.noBlockingHashInQueue(threadName)) {
            updateState();
        }

        events = navigator.getEventFleetList();
        if (events == null) return;
        putIncomingInArriveMap();
        sendCommandToCheckColonyWhenFleetArrives();
        markSpecialFleets();
    }

    private boolean isNavigatorExpired() {
        return navigator.isExpiredAfterMinutes(updateTimeInMinutes);
    }

    private void putIncomingInArriveMap() {
        events.stream()
                .filter(event -> LocalDateTime.now().isBefore(event.arrivalTime)) // remove not updated events
                .filter(event -> LocalDateTime.now().plusMinutes(updateTimeInMinutes).isAfter(event.arrivalTime))
                .filter(EventFleet::isFleetImpactOnColony) //is ending fly
                .filter(event -> !arrivedMap.containsKey(event.arrivalTime)) // only not registered yet
                .forEach(event -> arrivedMap.put(event.arrivalTime, event.getEndingPlanet()));
    }

    private void sendCommandToCheckColonyWhenFleetArrives() {
        HashMap<LocalDateTime, Planet> toVisit = new HashMap<>();
        arrivedMap.forEach((localDateTime, planet) -> {
            //plus 5sek because refreshed page is not updated
            if(LocalDateTime.now().minusSeconds(5).isAfter(localDateTime)) {
                toVisit.put(localDateTime, planet);
            }
        });
        toVisit.forEach((localDateTime, planet) -> {
            arrivedMap.remove(localDateTime, planet);
            Optional<ColonyEntity> optionalColony = ColonyDAO.getInstance().fetchAll().stream()
                    .filter(col -> col.is(ColonyType.PLANET) == ColonyType.PLANET.equals(planet.type))
                    .filter(col -> col.getCordinate().equals(Planet.getCordinate(planet)))
                    .findAny();
            if(optionalColony.isPresent()) {
//                new OpenPageCommand(FLEETDISPATCH, optionalColony.get()).push();
                new OpenPageCommand(FLEETDISPATCH, optionalColony.get());
            } else System.err.println("\nShould find colony "+planet.toString()+"\n");
        });
    }

    public static void updateState() {
        new LoadColoniesCommand()
                .hash(threadName+"_LoadColoniesCommand")
                .setRunType(QueueRunType.MAJOR)
                .push();
        new UpdateFleetEventsCommand()
                .hash(threadName+"_UpdateFleetEventsCommand")
                .setRunType(QueueRunType.MAJOR)
                .push();
    }

    private void markSpecialFleets() {
        CacheEntryDAO cacheEntryDAO = CacheEntryDAO.getInstance();
//        markMainFleet();

        ColonyEntity probeSwarmColony = ProbeSwarmFinder.find();
        cacheEntryDAO.setValue(PROBE_SWAM, probeSwarmColony != null ? probeSwarmColony.toString() : null);
    }

    private void markMainFleet() {
        ShipsMap noProbe = new ShipsMap();
        noProbe.put(ShipEnum.espionageProbe, 0L);

        List<Long> fleetCounts = ColonyDAO.getInstance().fetchAll().stream()
                .map(colony -> colony.getShipsMap().reduce(noProbe).count())
                .collect(Collectors.toList());
        events.stream()
                .filter(eventFleet -> !eventFleet.detailsFleet.trim().isEmpty())
                .map(event -> NumberUtil.toLong(event.detailsFleet))
                .collect(Collectors.toCollection(() -> fleetCounts));
        OptionalLong max = fleetCounts.stream().mapToLong(value -> value).max();
        if(max.isPresent()) {
            return;
        }
    }
}
