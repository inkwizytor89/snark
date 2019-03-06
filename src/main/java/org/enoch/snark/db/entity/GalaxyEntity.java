package org.enoch.snark.db.entity;

import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "galaxy", schema = "public", catalog = "snark")
public class GalaxyEntity extends BaseEntity {
    private Integer galaxy;
    private Integer system;
    private LocalDateTime updated;

    @Basic
    @Column(name = "galaxy")
    public Integer getGalaxy() {
        return galaxy;
    }

    public void setGalaxy(Integer galaxy) {
        this.galaxy = galaxy;
    }

    @Basic
    @Column(name = "system")
    public Integer getSystem() {
        return system;
    }

    public void setSystem(Integer system) {
        this.system = system;
    }

    @Basic
    @Column(name = "updated")
    public LocalDateTime getUpdated() {
        return updated;
    }

    public void setUpdated(LocalDateTime updated) {
        this.updated = updated;
    }

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

    @Override
    public String toString() {
        return "["+galaxy+", "+system+"]";
    }

    public SystemView toSystemView() {
        return new SystemView(galaxy, system);
    }
}
