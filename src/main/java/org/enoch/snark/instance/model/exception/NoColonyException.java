package org.enoch.snark.instance.model.exception;

import org.enoch.snark.instance.model.to.Planet;

public class NoColonyException extends LogicException {
    public NoColonyException(Planet planet) {
        super(planet+" not known");
    }
}
