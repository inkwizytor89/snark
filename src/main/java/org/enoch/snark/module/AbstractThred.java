package org.enoch.snark.module;

import org.enoch.snark.instance.SI;

public abstract class AbstractThred extends Thread {
    protected SI si;

    public AbstractThred(SI si) {
        this.si = si;
    }
}
