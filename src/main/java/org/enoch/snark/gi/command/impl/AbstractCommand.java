package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.gi.GI;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.model.types.QueueRunType;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.model.types.QueueRunType.FLEET_ACTION;
import static org.enoch.snark.instance.model.types.QueueRunType.INTERFACE_ACTION;

public abstract class AbstractCommand {
    protected final WebDriver webDriver;
    private FollowingAction followingAction;
    private QueueRunType runType = INTERFACE_ACTION;
    protected Instance instance;
    public int failed = 0;
    Integer priority = 100;
    private final List<String> tags = new ArrayList<>();


    protected AbstractCommand() {
        this.instance = Instance.getInstance();
        webDriver = GI.getInstance().getWebDriver();
        if(this instanceof SendFleetCommand) runType = FLEET_ACTION;
    }

    public abstract boolean execute();

    public void push() {
        Commander.getInstance().push(this);
    }

    public void push(String tag) {
        Commander.getInstance().push(this, tag);
    }

    public void doFallowing() {
        if(followingAction == null) {
            return;
        }
        new WaitingThread(followingAction).start();
    }

    public boolean isFollowingAction() {
        return followingAction != null;
    }

    public void retry(Integer secondsToDelay) {
        new WaitingThread(new FollowingAction(this, secondsToDelay)).start();
    }

    public FollowingAction setNext(AbstractCommand command, String... args) {
        return setNext(command, 0L, args);
    }

    public FollowingAction setNext(AbstractCommand command, Long delay, String... args) {
        FollowingAction followingAction = new FollowingAction(command, delay, args);
        this.followingAction = followingAction;
        return followingAction;
    }

    public void clearNext() {
        followingAction = null;
    }

    public FollowingAction getFollowingAction() {
        return followingAction;
    }

    public boolean isRequiredAction(String action) {
        return followingAction != null && followingAction.contains(action);
    }

    public void onInterrupt() {
    }

    public AbstractCommand addTag(String tag) {
        tags.add(tag);
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public QueueRunType getRunType() {
        return runType;
    }

    public void setRunType(QueueRunType runType) {
        this.runType = runType;
    }
}
