package org.enoch.snark.instance;

import org.enoch.snark.module.AbstractThread;

public interface SI {
    int getAvailableFleetCount(AbstractThread thred);
    Instance getInstance();
}
