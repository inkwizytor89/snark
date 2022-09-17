package org.enoch.snark.db.entity;

import org.enoch.snark.instance.Instance;
import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Entity
@Table(name = "galaxy", schema = "public", catalog = "snark")
public class GalaxyEntity extends IdEntity implements Comparable<GalaxyEntity> {

    @Basic
    @Column(name = "galaxy")
    public Integer galaxy;

    @Basic
    @Column(name = "system")
    public Integer system;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalaxyEntity that = (GalaxyEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(galaxy, that.galaxy) &&
                Objects.equals(system, that.system) &&
                Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, galaxy, system, updated);
    }

    public SystemView toSystemView() {
        return new SystemView(galaxy, system);
    }

    @Override
    public int compareTo(GalaxyEntity o) {
        return Integer.compare(distance(), o.distance());
    }

    private int distance() {
        List<Planet> cachedPlaned = Instance.getInstance().cachedPlaned;
        Optional<Planet> min = cachedPlaned.stream().min(Comparator.comparingInt(this::distance));
        if(min.isPresent()) {
            return distance(min.get());
        } else {
            return Integer.MAX_VALUE;
        }
    }

    private int distance(Planet planet) {
        return Math.abs(planet.galaxy - galaxy) * 1000 + Math.abs(planet.system - system);
    }

    @Override
    public String toString() {
        return "["+galaxy+", "+system+"]";
    }
}
