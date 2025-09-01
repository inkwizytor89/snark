package org.enoch.snark.action.command;

import lombok.Data;
import org.apache.commons.lang3.NotImplementedException;
import org.enoch.snark.action.command.status.CommandStatus;
import org.enoch.snark.action.command.status.ExecutionIssue;
import org.enoch.snark.action.command.status.ExecutionStatus;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.si.Core;
import org.enoch.snark.instance.si.QueueRunType;
import org.openqa.selenium.WebDriver;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.enoch.snark.action.command.status.ExecutionIssue.NO_ISSUE;
import static org.enoch.snark.action.command.status.ExecutionStatus.NEW;
import static org.enoch.snark.instance.si.QueueRunType.NORMAL;

@Data
public abstract class AbstractCommand {

    protected final CommandStatus status;
    protected WebDriver webDriver;
    private FollowingAction followingAction;
    public LocalDateTime from;
    private QueueRunType runType = NORMAL;
    protected Instance instance;
    private String hash;
    private final List<String> tags = new ArrayList<>();

    private QueueRunType queueRunType;


    protected AbstractCommand() {
        status = new CommandStatus(NEW, NO_ISSUE, 0);
//        this.instance = Instance.getInstance();
//        webDriver = GI.getInstance().getWebDriver();
//        if(this instanceof SendFleetCommand) runType = NORMAL;
    }


//    public abstract boolean execute();

    public void push(String action) {
        throw new NotImplementedException("To remove in spring version");
//        Consumer.getInstance().push(this, action);
    }

    public void push(LocalDateTime from) {
        throw new NotImplementedException("To remove in spring version");
//        Consumer.getInstance().push(this, from);
    }

    public void push() {
        throw new NotImplementedException("To remove in spring version");
//        Consumer.getInstance().push(this);
    }

    public boolean isFollowingAction() {
        return followingAction != null;
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

    public AbstractCommand queue(QueueRunType queueRunType){
        this.queueRunType = queueRunType;
        return this;
    }

    public QueueRunType getQueueRunType(){
        return queueRunType;
    }
}
