package org.enoch.snark.module;

import org.enoch.snark.instance.SI;

import java.util.concurrent.TimeUnit;

public abstract class AbstractThread extends Thread {
    protected SI si;

    public AbstractThread(SI si) {
        this.si = si;
        setName(this.getClass().getName());
    }

    protected abstract int getPauseInSeconds();

    protected void onStart() {

    }

    protected abstract void onStep();

    @Override
    public void run() {
        super.run();
        onStart();
        while(true) {
            try {
                onStep();
            } catch (Exception e) {
                e.printStackTrace();
            }
            si.getInstance().gi.sleep(TimeUnit.SECONDS, getPauseInSeconds());
        }
    }
}
