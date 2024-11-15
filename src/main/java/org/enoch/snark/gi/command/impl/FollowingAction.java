package org.enoch.snark.gi.command.impl;

import java.util.ArrayList;
import java.util.Collections;

import static org.enoch.snark.instance.model.types.FleetDirectionType.BACK;
import static org.enoch.snark.instance.model.types.FleetDirectionType.THERE;

public class FollowingAction extends ArrayList<String> {

    public static final String DELAY_TO_FLEET_BACK = BACK.name().toLowerCase();
    public static final String DELAY_TO_FLEET_THERE = THERE.name().toLowerCase();

    private final AbstractCommand command;
    public long secondsToDelay = 0;

    public FollowingAction(AbstractCommand command, long secondsToDelay, String... args) {
        super();
        this.command = command;
        this.secondsToDelay = secondsToDelay;
        Collections.addAll(this, args);
    }

    public AbstractCommand getCommand() {
        return command;
    }

    public long getSecondsToDelay() {
        return secondsToDelay;
    }

    public FollowingAction setSecondsToDelay(long secondsToDelay) {
        this.secondsToDelay = secondsToDelay;
        return this;
    }
}
