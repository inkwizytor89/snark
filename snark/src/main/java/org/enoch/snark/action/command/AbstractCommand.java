package org.enoch.snark.action.command;

import lombok.Data;
import org.enoch.snark.action.command.status.CommandStatus;
import org.enoch.snark.instance.si.QueueRunType;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.enoch.snark.action.command.status.ExecutionIssue.NO_ISSUE;
import static org.enoch.snark.action.command.status.ExecutionIssue.RETRY;
import static org.enoch.snark.action.command.status.ExecutionStatus.*;
import static org.enoch.snark.instance.si.QueueRunType.NORMAL;

@Data
public abstract class AbstractCommand {

    protected Long debugId;
    protected static Long debugIndex = 0L;
    protected final CommandStatus status;
    private FollowingAction followingAction;
    private QueueRunType runType = NORMAL;
    private String hash;
    private final List<String> tags = new ArrayList<>();

    private QueueRunType queueRunType;


    protected AbstractCommand() {
        status = new CommandStatus(NOT_REGISTERED, NO_ISSUE, null,0);
        hash = this.getClass().getSimpleName();
        debugId = ++debugIndex;
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

    public boolean notExecuted() {
        return asList(NEW, IN_PROGRESS, FAILED, WAITING, NOT_REGISTERED, RETRY).contains(getStatus().getStatus());
    }

    public boolean executed() {
        return asList(SUCCESS, CRASHED).contains(getStatus().getStatus());
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
