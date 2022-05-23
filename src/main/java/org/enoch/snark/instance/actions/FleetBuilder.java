package org.enoch.snark.instance.actions;

import org.enoch.snark.db.entity.FleetEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.SourceEntity;
import org.enoch.snark.instance.Instance;

public class FleetBuilder {

    private final Instance instance;
    private final SourceEntity source;
    private final PlanetEntity planet;
    private final FleetEntity fleet;

    public FleetBuilder(Instance instance, SourceEntity source, PlanetEntity planet) {
        fleet = new FleetEntity(instance);
        this.source = source;
        this.planet = planet;

    }
}
