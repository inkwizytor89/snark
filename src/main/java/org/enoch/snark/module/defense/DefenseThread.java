package org.enoch.snark.module.defense;

import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.text.Msg;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.macro.Mission.STATIONED;
import static org.enoch.snark.gi.text.Msg.BAZINGA_PL;

public class DefenseThread extends AbstractThread {

    public static final String ALARM = "alarm";
    public static final String threadName = "defense";
    public static final int UPDATE_TIME_IN_SECONDS = 10;

    private List<String> aggressorsAttacks = new ArrayList<>();
    private List<EventFleet> aggressorsEvents = new ArrayList<>();

    public DefenseThread() {
        super();
        setRunning(true);
    }

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
        if(!incomingAction.isEmpty()) {
            System.err.println("Send fleet to escape");
//            FleetEntity.createQuickColonization()
            return; //escape fleet
        }

    }

    private void loadAggressiveFleet() {
        aggressorsEvents = Navigator.getInstance().getEventFleetList().stream()
        .filter(event -> (event.isHostile && event.mission.isAggressive()) ||
                STATIONED.equals(event.mission))
        .collect(Collectors.toList());
    }

    private List<EventFleet> incomingAction(long aggressiveActionCount) {
        return aggressorsEvents.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(2+aggressiveActionCount).isAfter(event.arrivalTime))
                .collect(Collectors.toList());
    }

    private List<EventFleet> nearAction(long aggressiveActionCount) {
        return aggressorsEvents.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(2+aggressiveActionCount).isBefore(event.arrivalTime))
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
