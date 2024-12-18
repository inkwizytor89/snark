package org.enoch.snark.instance.si;

public enum QueueRunType {
    CRITICAL, // action for fleet escape
    MAJOR, // actions for attacks and checking debris
    NORMAL, // should be for not important action fleet: exp collect transport
    MINOR, // additional action like farm attack
    SPAM; // no database mas fleet start
    // database mas fleet start
}
