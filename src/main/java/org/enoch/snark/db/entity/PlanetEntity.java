package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "planets", schema = "public", catalog = "snark")
public class PlanetEntity {

    public final static String IN_ACTIVE = "IN_ACTIVE";

    private Long id;
    private Integer galaxy;
    private Integer system;
    private Integer position;
    private Integer power;
    private String type;
    private Long fleetSum;
    private Long defenseSum;
    private Timestamp updated;
    private UniverseEntity universeEntity;
    private Collection<SpyInfoEntity> spyInfosById;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",unique=true, nullable = false)
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
    @Column(name = "position")
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
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
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "fleet_sum")
    public Long getFleetSum() {
        return fleetSum;
    }

    public void setFleetSum(Long fleetSum) {
        this.fleetSum = fleetSum;
    }

    @Basic
    @Column(name = "defense_sum")
    public Long getDefenseSum() {
        return defenseSum;
    }

    public void setDefenseSum(Long defenseSum) {
        this.defenseSum = defenseSum;
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
        PlanetEntity entity = (PlanetEntity) o;
        return Objects.equals(id, entity.id) &&
                Objects.equals(galaxy, entity.galaxy) &&
                Objects.equals(system, entity.system) &&
                Objects.equals(position, entity.position) &&
                Objects.equals(power, entity.power) &&
                Objects.equals(type, entity.type) &&
                Objects.equals(fleetSum, entity.fleetSum) &&
                Objects.equals(defenseSum, entity.defenseSum) &&
                Objects.equals(updated, entity.updated);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, galaxy, system, position, power, type, fleetSum, defenseSum, updated);
    }

    @ManyToOne
    @JoinColumn(name = "universe_id", referencedColumnName = "id", nullable = false)
    public UniverseEntity getUniversesByUniverseId() {
        return universeEntity;
    }

    public void setUniversesByUniverseId(UniverseEntity universesByUniverseId) {
        this.universeEntity = universesByUniverseId;
    }

    @OneToMany(mappedBy = "planetsByPlanetId")
    public Collection<SpyInfoEntity> getSpyInfosById() {
        return spyInfosById;
    }

    public void setSpyInfosById(Collection<SpyInfoEntity> spyInfosById) {
        this.spyInfosById = spyInfosById;
    }
}
