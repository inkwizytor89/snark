package org.enoch.snark.instance;

import org.enoch.snark.module.AbstractThred;

public interface SI {
    int getAvailableFleetCount(AbstractThred thred);
    Instance getInstance();
}
