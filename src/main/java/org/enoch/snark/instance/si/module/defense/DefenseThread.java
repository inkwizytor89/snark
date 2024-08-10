package org.enoch.snark.instance.si.module.defense;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.gi.command.impl.SendFleetCommand;
import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.types.Mission;
import org.enoch.snark.gi.text.Msg;
import org.enoch.snark.instance.model.action.FleetBuilder;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.action.ColonyPlaner;
import org.enoch.snark.instance.model.to.EventFleet;
import org.enoch.snark.instance.model.to.Planet;
import org.enoch.snark.instance.si.module.AbstractThread;
import org.enoch.snark.instance.si.module.ConfigMap;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.enoch.snark.db.entity.FleetEntity.DEFENCE_CODE;
import static org.enoch.snark.gi.types.Mission.*;
import static org.enoch.snark.gi.text.Msg.BAZINGA_PL;
import static org.enoch.snark.instance.commander.QueueRunType.CRITICAL;
import static org.enoch.snark.instance.model.to.Resources.everything;

public class DefenseThread extends AbstractThread {

    public static final String threadType = "defense";
    public static final String ALARM = "alarm";
    public static final int UPDATE_TIME_IN_SECONDS = 10;
    public static final String LIMIT = "limit";

    private List<String> aggressorsAttacks = new ArrayList<>();
    private List<EventFleet> aggressorsEvents = new ArrayList<>();

    public DefenseThread(ConfigMap map) {
        super(map);
    }

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected int getPauseInSeconds() {
        return UPDATE_TIME_IN_SECONDS;
    }

    @Override
    public int getRequestedFleetCount() {
        return 2;
    }

    @Override
    protected void onStep() {
        loadAggressiveFleet();
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


        if(!incomingAction.isEmpty() && commander.noBlockingHashInQueue(threadType)) {
            incomingAction.forEach(eventFleet -> System.err.println("incomingAction from eventFleet "+eventFleet));
            Set<Planet> attackedPlanets = incomingAction.stream()
                    .map(EventFleet::getTo)
                    .collect(Collectors.toSet());
            attackedPlanets.forEach(this::sendFleetEscape);
            System.err.println("Send fleet to escape");
        }
    }

    private void sendFleetEscape(Planet sourcePlanet) {
        if(ColonyDAO.getInstance().fetchAll().size() > 1 ) {
            sendToAnotherMoon(sourcePlanet);
            return;
        }
        ColonyEntity sourceEntity = ColonyDAO.getInstance().find(sourcePlanet);
        if(sourceEntity.espionageProbe != null && sourceEntity.espionageProbe>0) {
            sendOnSpy(sourceEntity);
        } else {
            sendOnHold(sourceEntity, Planet.parse("p[1:126:12]"));
        }
    }

    private void sendOnHold(ColonyEntity sourceEntity, Planet target) {
        new FleetBuilder()
                .from(sourceEntity)
                .to(target.toString())
                .mission(STOP)
                .ships(ShipsMap.ALL_SHIPS)
                .resources(everything)
                .queue(CRITICAL)
                .buildOne()
                .push();
    }

    private void sendOnSpy(ColonyEntity sourceEntity) {
        Planet target = sourceEntity.toPlanet();
        target.position = 16;
        new FleetBuilder()
                .from(sourceEntity)
                .to(target.toString())
                .mission(SPY)
                .ships(ShipsMap.ALL_SHIPS)
                .resources(everything)
                .queue(CRITICAL)
                .buildOne()
                .push();
    }

    private void sendToAnotherMoon(Planet sourcePlanet) {
        System.err.println("Escape from planet "+ sourcePlanet);
        ColonyEntity sourceEntity = ColonyDAO.getInstance().find(sourcePlanet);
        System.err.println("Escape from colony "+sourceEntity.toPlanet() + " " + sourceEntity);
        ShipsMap shipsMap = sourceEntity.getShipsMap();
        if(shipsMap.isEmpty()) return;

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
        command.hash(threadType +sourceEntity);
        command.promise().setResources(everything);
        command.promise().setShipsMap(ShipsMap.ALL_SHIPS);
        command.setRunType(CRITICAL);
        command.push();
    }

    private ColonyEntity chooseDestination(Planet source) {
        List<ColonyEntity> destinationList = ColonyDAO.getInstance().fetchAll().stream()
                .filter(colony -> !colony.is(ColonyType.PLANET)).collect(Collectors.toList());
        if(destinationList.isEmpty()) {
            destinationList = ColonyDAO.getInstance().fetchAll().stream()
                    .filter(colony -> colony.is(ColonyType.PLANET)).collect(Collectors.toList());
        }

        destinationList = destinationList.stream()
                .filter(colony -> !colony.toPlanet().equals(source)).collect(Collectors.toList());

        return new ColonyPlaner(destinationList).getNearestColony(source);
    }

    private void loadAggressiveFleet() {
        Long limit = map.getConfigLong(LIMIT, 3000L);

        aggressorsEvents = Navigator.getInstance().getEventFleetList().stream()
        .filter(event -> (event.isHostile && event.mission.isAggressive())
//                || STATIONED.equals(event.mission)
        )
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
                    new SendMessageToPlayerCommand(event.sendMail, Msg.get(BAZINGA_PL)).push();
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
