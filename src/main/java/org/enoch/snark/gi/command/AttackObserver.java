package org.enoch.snark.gi.command;

import org.enoch.snark.model.WarInfo;

public interface AttackObserver {
    public void report(WarInfo info);
}
