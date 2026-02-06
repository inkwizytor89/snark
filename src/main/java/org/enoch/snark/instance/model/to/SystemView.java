package org.enoch.snark.instance.model.to;

import lombok.Data;

import java.util.Objects;

@Data
public class SystemView {
    public Integer galaxy;
    public Integer system;

    public SystemView(Planet planet) {
        this(planet.galaxy, planet.system);
    }

    public SystemView(Integer galaxy, Integer system) {
        this.galaxy = galaxy;
        this.system = system;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof SystemView)) {
            return false;
        }
        SystemView another = (SystemView) obj;
        return galaxy.equals(another.galaxy) && system.equals(another.system);
    }

    @Override
    public int hashCode() {
        return Objects.hash(galaxy, system);
    }

    @Override
    public String toString() {
        return "["+galaxy+":"+system+"]";
    }
}
