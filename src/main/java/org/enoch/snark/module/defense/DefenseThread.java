package org.enoch.snark.module.defense;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.gi.text.Msg;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.ColonyPlaner;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.Planet;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.enoch.snark.db.entity.FleetEntity.DEFENCE_CODE;
import static org.enoch.snark.gi.macro.Mission.*;
import static org.enoch.snark.gi.text.Msg.BAZINGA_PL;

public class DefenseThread extends AbstractThread {

    public static final String ALARM = "alarm";
    public static final String threadName = "defense";
    public static final int UPDATE_TIME_IN_SECONDS = 10;
    public static final String LIMIT = "limit";

    private List<String> aggressorsAttacks = new ArrayList<>();
    private List<EventFleet> aggressorsEvents = new ArrayList<>();

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return UPDATE_TIME_IN_SECONDS;
    }

    @Override
    public int getRequestedFleetCount() {
        return 1;
    }

    @Override
    protected void onStep() {
        loadAggressiveFleet();
        if(true) return ;
        if(aggressorsEvents.isEmpty()) {
            clearCache();
            return;
        }
        playMusic();
        long aggressiveActionCount = aggressorsEvents.size();

        List<EventFleet> nearAction = nearAction(aggressiveActionCount);
        System.err.println("nearAction "+nearAction.size());
//        if(!nearAction.isEmpty()) writeMessageToPlayer(nearAction);

        List<EventFleet> incomingAction = incomingAction(aggressiveActionCount);
        System.err.println("incomingAction "+ incomingAction.size());


        if(!incomingAction.isEmpty() && noWaitingElementsByTag(threadName)) {
            Set<Planet> attackedPlanets = incomingAction.stream()
                    .map(EventFleet::getEndingPlanet)
                    .collect(Collectors.toSet());
            attackedPlanets.forEach(this::sendFleetEscape);
            System.err.println("Send fleet to escape");
        }
    }

    private void sendFleetEscape(Planet sourcePlanet) {
        ColonyEntity sourceEntity = ColonyDAO.getInstance().get(sourcePlanet);
        Map<ShipEnum, Long> shipsMap = sourceEntity.getShipsMap();
        if(shipsMap.isEmpty())  return;

        FleetEntity fleetEntity = new FleetEntity();
        fleetEntity.source = sourceEntity;
        fleetEntity.setTarget(chooseDestination(sourcePlanet).toPlanet());
        fleetEntity.mission = STATIONED;
        fleetEntity.setShips(shipsMap);
        fleetEntity.metal = Long.MAX_VALUE;
        fleetEntity.crystal = Long.MAX_VALUE;
        fleetEntity.deuterium = Long.MAX_VALUE;
        fleetEntity.speed = 10L;
        fleetEntity.code = DEFENCE_CODE;

        SendFleetCommand command = new SendFleetCommand(fleetEntity);
        command.addTag(threadName);
        commander.push(command);
    }

    private ColonyEntity chooseDestination(Planet source) {
        List<ColonyEntity> destinationList = ColonyDAO.getInstance().fetchAll().stream()
                .filter(colony -> !colony.isPlanet).collect(Collectors.toList());
        if(destinationList.isEmpty()) {
            destinationList = ColonyDAO.getInstance().fetchAll().stream()
                    .filter(colony -> colony.isPlanet).collect(Collectors.toList());
        }

        destinationList = destinationList.stream()
                .filter(colony -> !colony.toPlanet().equals(source)).collect(Collectors.toList());

        return new ColonyPlaner(source, destinationList).getNearestColony();
    }

    private void loadAggressiveFleet() {
        Long limit = Instance.config.getConfigLong(threadName, LIMIT, 3000L);

        aggressorsEvents = Navigator.getInstance().getEventFleetList().stream()
        .filter(event -> (event.isHostile && event.mission.isAggressive()) ||
                STATIONED.equals(event.mission))
        .filter(eventFleet -> Long.parseLong(eventFleet.detailsFleet) > limit || DESTROY.equals(eventFleet.mission))
        .collect(Collectors.toList());
    }

    private List<EventFleet> incomingAction(long aggressiveActionCount) {
        return aggressorsEvents.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(1+aggressiveActionCount).isAfter(event.arrivalTime))
                .collect(Collectors.toList());
    }

    private List<EventFleet> nearAction(long aggressiveActionCount) {
        return aggressorsEvents.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(1+aggressiveActionCount).isBefore(event.arrivalTime))
                .collect(Collectors.toList());
    }

    private void playMusic() {
        AlarmSoundPlayer.start();
    }

    private void writeMessageToPlayer(List<EventFleet> events) {
        events.stream()
                .filter(event -> event.isHostile && Mission.ATTACK.equals(event.mission))
                .filter(event -> !aggressorsAttacks.contains(event.sendMail))
                .forEach(event -> {
                    commander.push(new SendMessageToPlayerCommand(event.sendMail, Msg.get(BAZINGA_PL)));
                    aggressorsAttacks.add(event.sendMail);
                });
    }

    private void clearCache() {
        aggressorsAttacks = new ArrayList<>();
        aggressorsEvents = new ArrayList<>();
        AlarmSoundPlayer.stop();
        return ;
    }

    private boolean isDestroyMoonFleet() {
        return false;
    }

    private boolean isAgressiveSpy() {
        return false;
    }

    private boolean isAttack() {
        return false;
    }

    private boolean isUnderAttack() {
        //isAttack() || isAgressiveSpy() || isDestroyMoonFleet();
        return aggressorsEvents.stream().anyMatch(event -> event.isHostile && Mission.ATTACK.equals(event.mission));
    }
}
