package org.enoch.snark.module.hunting;

import org.enoch.snark.db.entity.PlayerEntity;
import org.enoch.snark.gi.command.impl.SendMessageToPlayerCommand;
import org.enoch.snark.gi.macro.Mission;
import org.enoch.snark.gi.text.Msg;
import org.enoch.snark.instance.commander.Navigator;
import org.enoch.snark.model.EventFleet;
import org.enoch.snark.module.AbstractThread;
import org.enoch.snark.module.defense.AlarmSoundPlayer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.enoch.snark.gi.text.Msg.BAZINGA_PL;

public class HuntingThread extends AbstractThread {

    public static final String threadName = "hunting";
    public static final int UPDATE_TIME_IN_SECONDS = 10;

    private List<PlayerEntity> targets = new ArrayList<>();

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
        return 0;
    }

    @Override
    protected void onStep() {
        if(targets.isEmpty()) generateTargetsList();
    }

    private void generateTargetsList() {
        List<PlayerEntity> result = fetchPlayersFromDatabse();
        targets = result.stream().filter(playerEntity -> notSkippingPalyer(playerEntity)).collect(Collectors.toList());
    }

    private List<PlayerEntity> fetchPlayersFromDatabse() {
        return null;
    }

    private boolean notSkippingPalyer(PlayerEntity playerEntity) {
        return false;
    }

}
