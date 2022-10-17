package org.enoch.snark.module;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AbstractThread extends Thread {

    private static final Logger log = Logger.getLogger( AbstractThread.class.getName() );
    protected final Instance instance;
    protected final Commander commander;

    protected SI si;
    protected int pause = 0;

    public AbstractThread(SI si) {
        this.si = si;
        instance = si.getInstance();
        commander = instance.commander;
        setName(this.getClass().getName());
    }

    public abstract String getThreadName();

    protected abstract int getPauseInSeconds();

    protected void onStart() {
        log.info("Thread " + getThreadName() + " starting on " + si.getInstance().universe.name);
    }

    protected abstract void onStep();

    @Override
    public void run() {
        super.run();
        onStart();
        boolean wasSleeping = false;
        while(true) {
            try {
                onStep();
            } catch (Exception e) {
                e.printStackTrace();
            }
            // sleeping logic
            do {
                if(!si.getInstance().commander.isRunning()) {
                    log.info("Thread " + getThreadName() + " stopping on " + si.getInstance().universe.name);
                    wasSleeping = true;
                }
                SleepUtil.secondsToSleep(getPauseInSeconds());
            }while(!si.getInstance().commander.isRunning());
            if(wasSleeping) {
                log.info("Thread " + getThreadName() + " back to live on " + si.getInstance().universe.name);
                wasSleeping = false;
            }
        }
    }
}
