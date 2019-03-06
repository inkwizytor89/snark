package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "planets", schema = "public", catalog = "snark")
public class TargetEntity extends PlanetEntity {

    public final static String IN_ACTIVE = "IN_ACTIVE";

    private Integer power;
    private String type;
    private Long fleetSum;
    private Long defenseSum;
    private LocalDateTime updated;
    private Collection<SpyInfoEntity> spyInfosById;
    private Collection<FleetEntity> fleet;

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
        TargetEntity entity = (TargetEntity) o;
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

    @OneToMany(mappedBy = "planetsByPlanetId")
    public Collection<SpyInfoEntity> getSpyInfosById() {
        return spyInfosById;
    }

    public void setSpyInfosById(Collection<SpyInfoEntity> spyInfosById) {
        this.spyInfosById = spyInfosById;
    }

    @OneToMany(mappedBy = "target")
    public Collection<FleetEntity> getFleet() {
        return fleet;
    }

    public void setFleet(Collection<FleetEntity> fleet) {
        this.fleet = fleet;
    }

    public Integer calculateDistance(TargetEntity planet) {
        if(!galaxy.equals(planet.galaxy)) {
            return roundDistance(galaxy, planet.galaxy, 6) *20000;
        }
        if(!system.equals(planet.system)) {
            return roundDistance(system, planet.system, 499) *95 +2700;
        }
        return roundDistance(position, planet.position, 15)*5+1000;
    }

    private int roundDistance(Integer x1, Integer x2, Integer max) {
        return Math.abs(x1 - x2) < max - Math.abs(x1 - x2) ?  Math.abs(x1 - x2) : max - Math.abs(x1 - x2);
    }
}
