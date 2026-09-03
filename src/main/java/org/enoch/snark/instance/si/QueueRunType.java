package org.enoch.snark.instance.si;

public enum QueueRunType {
    CRITICAL, // action for fleet escape
    REFRESH_ACTION, // for refreshing planets - prio is so high to slot required action no block refresh and info that can already sent fleet
    MAJOR, // actions for attacks and checking debris
    NORMAL, // should be for not important action fleet: exp collect transport
    SPAM; // no database mas fleet start
    // database mas fleet start
}
