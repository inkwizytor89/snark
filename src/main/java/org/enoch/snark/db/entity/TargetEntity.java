package org.enoch.snark.db.entity;

import org.enoch.snark.model.exception.TargetMissingResourceInfoException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "targets", schema = "public", catalog = "snark")
public class TargetEntity extends PlanetEntity {

    public final static String ADMIN = "ADMIN";
    public final static String NORMAL = "NORMAL";
    public final static String ABSENCE = "ABSENCE";
    public final static String WEAK = "WEAK";
    public final static String IN_ACTIVE = "IN_ACTIVE";

    @Basic
    @Column(name = "type")
    public String type;

    @Basic
    @Column(name = "fleet_sum")
    public Long fleetSum;

    @Basic
    @Column(name = "defense_sum")
    public Long defenseSum;

    @Basic
    @Column(name = "spy_level")
    public Integer spyLevel;

    @Basic
    @Column(name = "updated")
    public LocalDateTime updated;

    public TargetEntity() {
        super();
    }

    public TargetEntity(String input) {
        super(input);
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

    public void update(TargetEntity targetEntity) {
        this.metal = targetEntity.metal;
        this.crystal = targetEntity.crystal;
        this.deuterium = targetEntity.deuterium;
        this.resources = 3 * metal + 2 * crystal + deuterium;
        this.power = targetEntity.power;

        this.fleetSum = targetEntity.fleetSum;
        this.defenseSum = targetEntity.defenseSum;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, galaxy, system, position, power, type, fleetSum, defenseSum, updated);
    }

    public Long calculateTransportByLt() {
        if (metal == null || crystal == null || deuterium == null) {
            throw new TargetMissingResourceInfoException();
        }
        return (long) Math.ceil((double) (metal+crystal+deuterium)/10000);
    }
}
