package org.enoch.snark.instance.model.uc;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.exception.NoPlanetEntityException;
import org.enoch.snark.instance.model.to.Planet;

import java.util.Optional;

public class PlanetUC {

    public static PlanetEntity fetch(Planet planet) {
        if(planet == null) return null;
        ColonyEntity colony = ColonyDAO.getInstance().find(planet);
        if(colony != null) return colony;
        Optional<TargetEntity> targetEntityOptional = TargetDAO.getInstance().find(planet);
        if(targetEntityOptional.isPresent()) return targetEntityOptional.get();
        else throw new NoPlanetEntityException(planet);
    }
}
