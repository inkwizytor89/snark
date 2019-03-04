package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@Table(name = "spy_info", schema = "public", catalog = "snark")
public class SpyInfoEntity extends BaseEntity {
    private Integer metal;
    private Integer crystal;
    private Integer deuterium;
    private Integer power;
    private Timestamp update;
    private PlanetEntity planetsByPlanetId;
    private SourceEntity sourcesBySourceId;

    @Basic
    @Column(name = "metal")
    public Integer getMetal() {
        return metal;
    }

    public void setMetal(Integer metal) {
        this.metal = metal;
    }

    @Basic
    @Column(name = "crystal")
    public Integer getCrystal() {
        return crystal;
    }

    public void setCrystal(Integer crystal) {
        this.crystal = crystal;
    }

    @Basic
    @Column(name = "deuterium")
    public Integer getDeuterium() {
        return deuterium;
    }

    public void setDeuterium(Integer deuterium) {
        this.deuterium = deuterium;
    }

    @Basic
    @Column(name = "power")
    public Integer getPower() {
        return power;
    }

    public void setPower(Integer power) {
        this.power = power;
    }

    @Basic
    @Column(name = "update")
    public Timestamp getUpdate() {
        return update;
    }

    public void setUpdate(Timestamp update) {
        this.update = update;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SpyInfoEntity that = (SpyInfoEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(metal, that.metal) &&
                Objects.equals(crystal, that.crystal) &&
                Objects.equals(deuterium, that.deuterium) &&
                Objects.equals(power, that.power) &&
                Objects.equals(update, that.update);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, metal, crystal, deuterium, power, update);
    }

    @ManyToOne
    @JoinColumn(name = "planet_id", referencedColumnName = "id", nullable = false)
    public PlanetEntity getPlanetsByPlanetId() {
        return planetsByPlanetId;
    }

    public void setPlanetsByPlanetId(PlanetEntity planetsByPlanetId) {
        this.planetsByPlanetId = planetsByPlanetId;
    }

    @ManyToOne
    @JoinColumn(name = "source_id", referencedColumnName = "id")
    public SourceEntity getSourcesBySourceId() {
        return sourcesBySourceId;
    }

    public void setSourcesBySourceId(SourceEntity sourcesBySourceId) {
        this.sourcesBySourceId = sourcesBySourceId;
    }
}
