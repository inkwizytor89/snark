package org.enoch.snark.module;

import org.enoch.snark.instance.SI;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public abstract class AbstractThread extends Thread {

    private static final Logger log = Logger.getLogger( AbstractThread.class.getName() );

    protected SI si;

    public AbstractThread(SI si) {
        this.si = si;
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
                si.getInstance().gi.sleep(TimeUnit.SECONDS, 10);
            }while(!si.getInstance().commander.isRunning());
            if(wasSleeping) {
                log.info("Thread " + getThreadName() + " back to live on " + si.getInstance().universe.name);
                wasSleeping = false;
            }
        }
    }
}
