package org.enoch.snark.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "exploration", schema = "public", catalog = "snark")
public class ExplorationEntity {
    private long id;
    private Integer galaxy;
    private Integer system;
    private Timestamp updated;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

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
    public Timestamp getUpdated() {
        return updated;
    }

    public void setUpdated(Timestamp updated) {
        this.updated = updated;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ExplorationEntity that = (ExplorationEntity) o;
        return id == that.id &&
                Objects.equals(galaxy, that.galaxy) &&
                Objects.equals(system, that.system) &&
                Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, galaxy, system, updated);
    }
}
