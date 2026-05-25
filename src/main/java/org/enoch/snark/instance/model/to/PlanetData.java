package org.enoch.snark.instance.model.to;

import lombok.Data;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.db.entity.PlanetEntity;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.Coordinate;

@Data
public class PlanetData implements Coordinate {
    @Override
    public Planet toCoordinate() {
        return planet;
    }

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

    @Override
    public String toString() {
        return planet.toString();
    }
}
