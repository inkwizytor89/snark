package org.enoch.snark.action.command.status;

public enum ExecutionIssue {
    NO_ISSUE,
    RETRY,
    TO_STRONG_PLAYER,
    TO_WEAK_PLAYER,
    CAN_NOT_SENT,
    CONDITION_WONT_FIT,
    NOT_ENOUGH_DEUTERIUM,
    FLEET_IN_COMBAT,
    OTHER
}
