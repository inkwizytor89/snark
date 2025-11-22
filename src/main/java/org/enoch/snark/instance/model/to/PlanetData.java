package org.enoch.snark.instance.model.to;

import lombok.Data;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;

@Data
public class PlanetData {
    private Planet planet;
    private ColonyEntity colony;
    private TargetEntity target;

    public PlanetData(ColonyEntity planet) {
        this.planet = planet.toPlanet();
        this.colony = planet;
    }

    public PlanetData(TargetEntity planet) {
        this.planet = planet.toPlanet();
        this.target = planet;
    }

    public PlanetEntity planetData() {
        if(colony != null) return colony;
        if(target != null) return target;
        return null;
    }

    public boolean is() {
        return colony != null || target != null;
    }
}
