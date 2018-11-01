package org.enoch.snark.gi.command;

import org.enoch.snark.model.SpyInfo;

public interface SpyObserver {
    public void report(SpyInfo info);
}
