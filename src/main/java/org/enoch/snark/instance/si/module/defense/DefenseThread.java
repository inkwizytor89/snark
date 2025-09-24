package org.enoch.snark.instance.si.module.defense;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.*;
import org.enoch.snark.common.time.Duration;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.to.*;
import org.enoch.snark.instance.service.FleetDispatcher;
import org.enoch.snark.instance.si.module.consumer.gi.types.Mission;
import org.enoch.snark.instance.si.module.consumer.gi.text.Msg;
import org.enoch.snark.instance.model.types.ColonyType;
import org.enoch.snark.instance.model.types.FleetDirectionType;
import org.enoch.snark.instance.service.Navigator;
import org.enoch.snark.instance.model.action.ColonyPlaner;
import org.enoch.snark.instance.si.module.AbstractThread;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;
import static org.enoch.snark.instance.si.QueueRunType.CRITICAL;
import static org.enoch.snark.instance.si.module.ThreadMap.RECALL;
import static org.enoch.snark.instance.si.module.consumer.gi.text.Msg.BAZINGA_PL;
import static org.enoch.snark.instance.model.to.Resources.everything;
import static org.enoch.snark.instance.si.module.consumer.gi.types.Mission.*;
import static org.enoch.snark.instance.si.module.defense.AlarmSoundPlayer.MISSING_WAV;

@RequiredArgsConstructor
public class DefenseThread extends AbstractThread {

    public static final String threadType = "defense";
    public static final String ALARM = "alarm";
    public static final int UPDATE_TIME_IN_SECONDS = 10;
    public static final String LIMIT = "limit";
    public static final String EXAMPLE_TIME = "example_time";
    public static final String EXAMPLE_COORDINATE = "example_coordinate";
    public static final String EXAMPLE_ATTACKER_COORDINATE = "example_attacker_coordinate";

    private final FleetDispatcher fleetDispatcher;

    private List<String> aggressorsAttacks = new ArrayList<>();
    private List<EventFleet> aggressorsEvents = new ArrayList<>();
    private int nearActionCont;
    private int incomingActionCont;

    @Override
    protected String getThreadType() {
        return threadType;
    }

    @Override
    protected String defaultPause() {
        return UPDATE_TIME_IN_SECONDS + "S";
    }

    @Override
    public int getRequestedFleetCount() {
        return 2;
    }

    @Override
    protected void onStep() {
        loadAggressiveFleet();
        if (aggressorsEvents.isEmpty()) {
            clearCache();
            return;
        }
        playMusic();
        long aggressiveActionCount = aggressorsEvents.size();

        List<EventFleet> nearAction = nearAction(aggressiveActionCount);
        if(nearActionCont != nearAction.size()) {
            nearActionCont = nearAction.size();
            System.err.println("nearAction " + nearActionCont + " at "+LocalTime.now());
//        if(!nearAction.isEmpty()) writeMessageToPlayer(nearAction);
        }

        List<EventFleet> incomingAction = incomingAction(aggressiveActionCount);
        if(incomingActionCont != incomingAction.size()) {
            incomingActionCont = incomingAction.size();
            System.err.println("incomingAction " + incomingActionCont + " at "+LocalTime.now());
        }

        if (!incomingAction.isEmpty()) {
            incomingAction.forEach(eventFleet -> System.err.println("incomingAction from eventFleet " + eventFleet));
            Set<ColonyEntity> attackedPlanets = incomingAction.stream()
                    .map(eventFleet -> colonyRepository.byPlanet(eventFleet.getTo()))
                    .filter(colonyEntity -> colonyEntity.getShipsMap().count() > 0)
                    .collect(Collectors.toSet());

            List<AbstractCommand> fleetToEscape = attackedPlanets.stream()
                    .filter(colonyEntity -> !colonyEntity.getShipsMap().isEmpty())
                    .map(colony -> {
                        List<EventFleet> events = incomingAction.stream()
                                .filter(eventFleet -> eventFleet.getTo().equals(colony.toPlanet())).toList();
                        return sendFleetEscape(colony, events);
                    })
                    .toList();
            noDuplicationPushCommands(fleetToEscape);
        }
    }

    private AbstractCommand sendFleetEscape(ColonyEntity source, List<EventFleet> eventFleets) {
        Duration recall = map.getDuration(RECALL, null);
        FleetSendCommand sendCommand = null;

        if (isMoreColonies()) {
            sendCommand = fleetToAnotherMoon(source);
        } else if (isEspionageProbe(source)) {
            sendCommand = fleetOnSpy(source);
        } else {
            if (recall == null)
                recall = new Duration("350S?50S");
            EventFleet eventFleet = eventFleets.getFirst();
            Planet target = new Planet(eventFleet.coordsOrigin, eventFleet.originFleet);
            sendCommand = sendOnAttacker(source, target);
        }

        if (recall != null) sendCommand.setNext(new RecallCommand(sendCommand), recall.getValue().getSeconds());

        sendCommand.queue(CRITICAL);
        return sendCommand;
    }

    private static boolean isEspionageProbe(ColonyEntity sourceEntity) {
        return sourceEntity.espionageProbe != null && sourceEntity.espionageProbe > 0;
    }

    private boolean isMoreColonies() {
        return colonyRepository.findAll().size() > 1;
    }

    private FleetSendCommand sendOnAttacker(ColonyEntity source, Planet target) {
        FleetSendCommand sendCommand = new FleetSendCommand();
        sendCommand.setSource(source);
        sendCommand.setTarget(target);
        sendCommand.setMission(ATTACK);
        sendCommand.setShipsMap(ALL_SHIPS);
        sendCommand.setResources(everything);
        sendCommand.setSpeed(10L);

        sendCommand.setHash(threadType +"_"+ source);
        sendCommand.setRunType(CRITICAL);

        log("Escape from planet " + source);
        return sendCommand;
    }

    private FleetSendCommand fleetOnSpy(ColonyEntity source) {
        Planet target = source.toPlanet();
        target.position = 16;

        FleetSendCommand sendCommand = new FleetSendCommand();
        sendCommand.setSource(source);
        sendCommand.setTarget(target);
        sendCommand.setMission(SPY);
        sendCommand.setShipsMap(ALL_SHIPS);
        sendCommand.setResources(everything);
        sendCommand.setSpeed(10L);

        sendCommand.setHash(threadType +"_"+ source);
        sendCommand.setRunType(CRITICAL);

        log("Escape from planet " + source);
        return sendCommand;
    }

    private FleetSendCommand fleetToAnotherMoon(ColonyEntity source) {
        FleetSendCommand sendCommand = new FleetSendCommand();
        sendCommand.setSource(source);
        sendCommand.setTarget(chooseDestination(source.toPlanet()).toPlanet());
        sendCommand.setMission(STATIONED);
        sendCommand.setShipsMap(ALL_SHIPS);
        sendCommand.setResources(everything);
        sendCommand.setSpeed(10L);

        sendCommand.setHash(threadType +"_"+ source);
        sendCommand.setRunType(CRITICAL);

        log("Escape from planet " + source);
        return sendCommand;
    }

    private ColonyEntity chooseDestination(Planet source) {
        List<ColonyEntity> destinationList = colonyRepository.findAll().stream()
                .filter(colony -> !colony.is(ColonyType.PLANET)).collect(Collectors.toList());
        if (destinationList.isEmpty()) {
            destinationList = colonyRepository.findAll().stream()
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

        putExampleFromConfig(limit);
    }

    private void putExampleFromConfig(Long limit) {
        LocalTime exampleTime = map.getLocalTime(EXAMPLE_TIME, null);
        if (exampleTime == null) return;

        EventFleet exampleFleet = new EventFleet();
        exampleFleet.isHostile = true;
        exampleFleet.iconMovement = FleetDirectionType.THERE;
        exampleFleet.detailsFleet = limit.toString();

        LocalDateTime time = LocalDateTime.now();
        time.withHour(exampleTime.getHour());
        time.withMinute(exampleTime.getMinute());
        exampleFleet.arrivalTime = time;

        Planet destOrigin = map.getConfigPlanet(EXAMPLE_COORDINATE);
        exampleFleet.destCoords = destOrigin.toString().substring(1);
        exampleFleet.destFleet = destOrigin.type;

        Planet coordsOrigin = map.getConfigPlanet(EXAMPLE_ATTACKER_COORDINATE);
        if(coordsOrigin == null) coordsOrigin = new Planet("p[1:1:1]");
        exampleFleet.coordsOrigin = coordsOrigin.toString();
        exampleFleet.originFleet = coordsOrigin.type;

        aggressorsEvents.add(exampleFleet);

    }

    private List<EventFleet> incomingAction(long aggressiveActionCount) {
        return aggressorsEvents.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(1 + aggressiveActionCount).isAfter(event.arrivalTime))
                .collect(Collectors.toList());
    }

    private List<EventFleet> nearAction(long aggressiveActionCount) {
        return aggressorsEvents.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(1 + aggressiveActionCount).isBefore(event.arrivalTime))
                .collect(Collectors.toList());
    }

    private void playMusic() {
        AlarmSoundPlayer.start(map.getConfig(ALARM, MISSING_WAV));
    }

    private void writeMessageToPlayer(List<EventFleet> events) {
        throw new NotImplementedException("To remove in spring version"); // missing push
//        events.stream()
//                .filter(event -> event.isHostile && Mission.ATTACK.equals(event.mission))
//                .filter(event -> !aggressorsAttacks.contains(event.sendMail))
//                .forEach(event -> {
//                    new SendMessageToPlayerCommand(event.sendMail, Msg.get(BAZINGA_PL));
//                    aggressorsAttacks.add(event.sendMail);
//                });
    }

    private void clearCache() {
        aggressorsAttacks = new ArrayList<>();
        aggressorsEvents = new ArrayList<>();
        AlarmSoundPlayer.stop();
        return;
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
