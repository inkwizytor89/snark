package org.enoch.snark.module.update;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;

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
        if(isNavigatorExpired() && noWaitingElementsByTag(threadName)) {
            sendCommandToUpdateEventFleets();
        }

        events = navigator.getEventFleetList();
        if (events == null) return;
        putIncomingInArriveMap();
        sendCommandToCheckColonyWhenFleetArrives();
    }

    private boolean isNavigatorExpired() {
        return navigator.isExpiredAfterMinutes(updateTimeInMinutes);
    }

    private void sendCommandToUpdateEventFleets() {
        OpenPageCommand command = new OpenPageCommand(PAGE_BASE_FLEET).setCheckEventFleet(true);
        command.addTag(threadName);
        command.push();
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
            //plus 4sek because refreshed page is not updated
            if(LocalDateTime.now().minusSeconds(4).isAfter(localDateTime)) {
                toVisit.put(localDateTime, planet);
            }
        });
        toVisit.forEach((localDateTime, planet) -> {
            arrivedMap.remove(localDateTime, planet);
            Optional<ColonyEntity> optionalColony = ColonyDAO.getInstance().fetchAll().stream()
                    .filter(col -> col.isPlanet == ColonyType.PLANET.equals(planet.type))
                    .filter(col -> col.getCordinate().equals(Planet.getCordinate(planet)))
                    .findAny();
            if(optionalColony.isPresent()) {
                new OpenPageCommand(PAGE_BASE_FLEET, optionalColony.get()).push();
            } else System.err.println("\nShould find colony "+planet.toString()+"\n");
        });
    }
}
