package org.enoch.snark.instance.model.exception;

import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.model.to.Planet;

public class NoPlanetEntityException extends IllegalStateException {
    public NoPlanetEntityException(Planet planet) {
        super("Missing "+ PlanetEntity.class.getName()+" for "+planet);
    }
}
