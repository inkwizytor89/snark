package org.enoch.snark.instance.commander;

public enum QueueRunType {
    CRITICAL, // action for fleet escape
    MAJOR, // actions for attacks and checking debris
    NORMAL, // should be for not important action fleet: exp collect transport
    MINOR; // should run without required fleet slot - only interface
    // database mas fleet start
}
