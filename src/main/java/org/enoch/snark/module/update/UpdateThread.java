package org.enoch.snark.module.update;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.command.impl.OpenPageCommand;
import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.text.Msg;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.instance.config.Config;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.types.ColonyType;
import org.enoch.snark.model.types.MissionType;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.enoch.snark.gi.macro.GIUrlBuilder.PAGE_BASE_FLEET;
import static org.enoch.snark.gi.text.Msg.BAZINGA_PL;

public class UpdateThread extends AbstractThread {

    public static final String threadName = "update";
    public static final long UPDATE_TIME_IN_MINUTES = 6L;

    private final Navigator navigator;
    private List<EventFleet> events;
    private final HashMap<LocalDateTime, Planet> arrivedMap = new HashMap<>();

    private final List<LocalDateTime> aggressorsAttacks = new ArrayList<>();

    public UpdateThread() {
        super();
        navigator = Navigator.getInstance();
        pause = 10;
        setRunning(true);
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
        if(isNavigatorExpired() && noWaitingElementsByTag(threadName)) {
            sendCommandToUpdateEventFleets();
        }

        events = navigator.getEventFleetList();
        if (events == null) return;
        String config = Instance.config.getConfig(Config.DEFENSE);
        boolean shouldDefenceActivate = config == null || config.isEmpty() || config.equals("on");
        if(shouldDefenceActivate && isUnderAttack()) {
            writeMessageToPlayer();
        }
        putIncomingInArriveMap();
        sendCommandToCheckColonyWhenFleetArrives();
    }

    private boolean isNavigatorExpired() {
        return navigator.getLastUpdate().plusMinutes(UPDATE_TIME_IN_MINUTES).isBefore(LocalDateTime.now());
    }

    private boolean isUnderAttack() {
        return events.stream().anyMatch(event -> event.isHostile && MissionType.ATTACK.equals(event.missionType));
    }

    private void writeMessageToPlayer() {
        events.stream()
                .filter(event -> event.isHostile && MissionType.ATTACK.equals(event.missionType))
                .filter(event -> !aggressorsAttacks.contains(event.arrivalTime))
                .forEach(event -> {
                    commander.push(new SendMessageToPlayerCommand(event.sendMail, Msg.get(BAZINGA_PL)));
                    aggressorsAttacks.add(event.arrivalTime);
                });
    }

    private void sendCommandToUpdateEventFleets() {
        OpenPageCommand command = new OpenPageCommand(PAGE_BASE_FLEET, null).setCheckEventFleet(true);
        command.addTag(threadName);
        commander.push(command);
    }

    private void putIncomingInArriveMap() {
        events.stream()
                .filter(event -> LocalDateTime.now().isBefore(event.arrivalTime)) // remove not updated events
                .filter(event -> LocalDateTime.now().plusMinutes(UPDATE_TIME_IN_MINUTES).isAfter(event.arrivalTime))
                .filter(EventFleet::isFleetImpactOnColony) //is ending fly
                .filter(event -> !arrivedMap.containsKey(event.arrivalTime)) // only not registered yet
                .forEach(event -> arrivedMap.put(event.arrivalTime, event.getEndingPlanet()));
    }

    private void sendCommandToCheckColonyWhenFleetArrives() {
        HashMap<LocalDateTime, Planet> toVisit = new HashMap<>();
        arrivedMap.forEach((localDateTime, planet) -> {
            //plus 2sek because refreshed page is not updated
            if(LocalDateTime.now().minusSeconds(3).isAfter(localDateTime)) {
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
                commander.push(new OpenPageCommand(PAGE_BASE_FLEET, optionalColony.get()));
            } else System.err.println("\nShould find colony "+planet.toString()+"\n");
        });
    }
}
