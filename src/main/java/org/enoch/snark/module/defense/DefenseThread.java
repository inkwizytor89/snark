package org.enoch.snark.module.defense;

import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.text.Msg;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.instance.config.Config;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.model.types.MissionType;
import org.enoch.snark.module.AbstractThread;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.text.Msg.BAZINGA_PL;

public class DefenseThread extends AbstractThread {

    public static final String threadName = "defense";
    public static final int UPDATE_TIME_IN_SECONDS = 10;

    private List<String> aggressorsAttacks = new ArrayList<>();
    private List<EventFleet> events = new ArrayList<>();

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
        if(!isDefenseActivate()) return;

        loadAggressiveFleet();

        if(!isUnderAttack()) {
            clearCache();
            return;
        }
        playMusic();

        List<EventFleet> moreThan4min = moreThan4min();
        if(!moreThan4min.isEmpty()) writeMessageToPlayer(moreThan4min);

        List<EventFleet> lessThan4min = lessThan4min();
        if(!lessThan4min.isEmpty()) {
            System.err.println("Send fleet to escape");
            return; //escape fleet
        }

    }

    private void loadAggressiveFleet() {
        events = Navigator.getInstance().getEventFleetList().stream()
        .filter(event -> event.isHostile && event.missionType.isAggressive())
        .collect(Collectors.toList());
    }

    private List<EventFleet> lessThan4min() {
        return events.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(4).isAfter(event.arrivalTime))
                .collect(Collectors.toList());
    }

    private List<EventFleet> moreThan4min() {
        return events.stream()
                .filter(event -> LocalDateTime.now().plusMinutes(4).isBefore(event.arrivalTime))
                .collect(Collectors.toList());
    }

    private void playMusic() {

    }

    private void writeMessageToPlayer(List<EventFleet> events) {
        events.stream()
                .filter(event -> event.isHostile && MissionType.ATTACK.equals(event.missionType))
                .filter(event -> !aggressorsAttacks.contains(event.sendMail))
                .forEach(event -> {
                    commander.push(new SendMessageToPlayerCommand(event.sendMail, Msg.get(BAZINGA_PL)));
                    aggressorsAttacks.add(event.sendMail);
                });
    }

    private void clearCache() {
        aggressorsAttacks = new ArrayList<>();
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
        return events.stream().anyMatch(event -> event.isHostile && MissionType.ATTACK.equals(event.missionType));
    }

    private boolean isDefenseActivate() {
        String config = Instance.config.getConfig(Config.DEFENSE);
        return config == null || config.isEmpty() || config.equals("on");
    }
}
