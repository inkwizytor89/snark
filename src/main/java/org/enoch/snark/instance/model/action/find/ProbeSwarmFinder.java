package org.enoch.snark.instance.model.action.find;

import org.enoch.snark.db.dao.CacheEntryDAO;
import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.Instance;

import java.util.List;
import java.util.Optional;

import static org.enoch.snark.instance.model.action.PlanetExpression.PROBE_SWAM;
import static org.enoch.snark.instance.si.module.ConfigMap.PROBE_SWAM_LIMIT;

public class ProbeSwarmFinder {

    public static ColonyEntity find() {
        long probeLimit = Instance.getMainConfigMap().getConfigNumber(PROBE_SWAM_LIMIT, "100k");

        String probeSwarmPlanetCode = CacheEntryDAO.getInstance().getValue(PROBE_SWAM);
        ColonyDAO colonyDAO = ColonyDAO.getInstance();
        ColonyEntity probeSwarmColony = probeSwarmPlanetCode != null ? colonyDAO.get(probeSwarmPlanetCode) : null;
        if(probeSwarmColony != null && probeSwarmColony.espionageProbe >= probeLimit) {
            return probeSwarmColony;
        }

        List<ColonyEntity> colonies = colonyDAO.fetchAll();
        Optional<ColonyEntity> any = colonies.stream()
                .filter(colony -> colony.espionageProbe >= probeLimit)
                .findAny();
        return any.orElse(null);
    }
}
