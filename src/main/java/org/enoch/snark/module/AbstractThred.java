package org.enoch.snark.module;

import org.enoch.snark.instance.SI;

import java.util.concurrent.TimeUnit;

public abstract class AbstractThred extends Thread {
    protected SI si;

    public AbstractThred(SI si) {
        this.si = si;
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
            si.getInstance().session.sleep(TimeUnit.SECONDS, getPauseInSeconds());
        }
    }
}
