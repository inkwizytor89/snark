package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "targets", schema = "public", catalog = "snark")
public class TargetEntity extends PlanetEntity {

    public final static String IN_ACTIVE = "IN_ACTIVE";
    public final static String WEAK = "WEAK";
    public final static String STRONG = "STRONG";

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
    @Column(name = "updated")
    public LocalDateTime updated;

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

    public Long calculateTransportByLt() {
        return (long) Math.ceil((double) (metal+crystal+deuterium)/10000);
    }
}
