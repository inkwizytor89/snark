package org.enoch.snark.gi.command.impl;

import org.enoch.snark.common.WaitingThread;
import org.enoch.snark.gi.GI;
import org.enoch.snark.instance.commander.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.commander.QueueRunType;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.instance.commander.QueueRunType.NORMAL;
import static org.enoch.snark.instance.commander.QueueRunType.MINOR;

public abstract class AbstractCommand {
    protected final WebDriver webDriver;
    private FollowingAction followingAction;
    private QueueRunType runType = NORMAL;
    protected Instance instance;
    public int failed = 0;
    private String hash;
    private final List<String> tags = new ArrayList<>();


    protected AbstractCommand() {
        this.instance = Instance.getInstance();
        webDriver = GI.getInstance().getWebDriver();
        if(this instanceof SendFleetCommand) runType = NORMAL;
    }

    public abstract boolean execute();

    public void push(LocalDateTime from) {
        Commander.getInstance().push(this, from);
    }

    public void push() {
        Commander.getInstance().push(this);
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

    public AbstractCommand hash(String hash) {
        this.hash = hash;
        tags.add(hash);
        return this;
    }

    public String hash() {
        return hash;
    }

    public AbstractCommand addNoneHashTag(String tag) {
        tags.add(tag);
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public QueueRunType getRunType() {
        return runType;
    }

    public AbstractCommand setRunType(QueueRunType runType) {
        if(runType != null)
            this.runType = runType;
        return this;
    }
}
