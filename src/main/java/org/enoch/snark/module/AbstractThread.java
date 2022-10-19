package org.enoch.snark.module;

import org.enoch.snark.common.SleepUtil;
import org.enoch.snark.instance.Commander;
import org.enoch.snark.instance.Instance;
import org.enoch.snark.instance.SI;

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
        commander = Commander.getInstance();
        setName(this.getClass().getName());
    }

    public abstract String getThreadName();

    protected abstract int getPauseInSeconds();

    protected void onStart() {
        log.info("Thread " + getThreadName() + " starting");
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
                if(!commander.isRunning()) {
                    log.info("Thread " + getThreadName() + " stopping");
                    wasSleeping = true;
                }
                SleepUtil.secondsToSleep(getPauseInSeconds());
            }while(!commander.isRunning());
            if(wasSleeping) {
                log.info("Thread " + getThreadName() + " back to live");
                wasSleeping = false;
            }
        }
    }
}
