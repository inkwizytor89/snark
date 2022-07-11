package org.enoch.snark.instance.actions;

import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.instance.Instance;

public class FleetBuilder {

    private final Instance instance = null;
    private final ColonyEntity source;
    private final PlanetEntity planet;
    private final FleetEntity fleet;

    public FleetBuilder(Instance instance, ColonyEntity source, PlanetEntity planet) {
        fleet = new FleetEntity(instance);
        this.source = source;
        this.planet = planet;

    }
}
