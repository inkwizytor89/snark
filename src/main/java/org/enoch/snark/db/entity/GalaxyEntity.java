package org.enoch.snark.db.entity;

import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "galaxy", schema = "public", catalog = "snark")
public class GalaxyEntity {
    private Long id;
    private Integer galaxy;
    private Integer system;
    private Timestamp updated;
    private UniverseEntity universesByUniversId;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
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

    @ManyToOne
    @JoinColumn(name = "univers_id", referencedColumnName = "id")
    public UniverseEntity getUniversesByUniversId() {
        return universesByUniversId;
    }

    public void setUniversesByUniversId(UniverseEntity universesByUniversId) {
        this.universesByUniversId = universesByUniversId;
    }

    public SystemView toSystemView() {
        return new SystemView(galaxy, system);
    }
}
