package org.enoch.snark.module.collector;

import org.enoch.snark.module.AbstractThread;

public class CollectorThread extends AbstractThread {

    public static final String threadName = "Collector";

    @Override
    public String getThreadName() {
        return threadName;
    }

    @Override
    protected int getPauseInSeconds() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int getRequestedFleetCount() {
        return 1;
    }

    @Override
    protected void onStep() {
    }
}
