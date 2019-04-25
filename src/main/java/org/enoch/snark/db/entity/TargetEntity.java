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

        this.lm = targetEntity.lm;
        this.cm = targetEntity.cm;
        this.kr = targetEntity.kr;
        this.ow = targetEntity.ow;
        this.pan = targetEntity.pan;
        this.bom = targetEntity.bom;
        this.ni = targetEntity.ni;
        this.gs = targetEntity.gs;
        this.mt = targetEntity.mt;
        this.dt = targetEntity.dt;
        this.kol = targetEntity.kol;
        this.rec = targetEntity.rec;
        this.son = targetEntity.son;
        this.sat = targetEntity.sat;

        this.defWr = targetEntity.defWr;
        this.defLdl = targetEntity.defLdl;
        this.defCdl = targetEntity.defCdl;
        this.defDg = targetEntity.defDg;
        this.defDj = targetEntity.defDj;
        this.defWp = targetEntity.defWp;
        this.defMpo = targetEntity.defMpo;
        this.defDpo = targetEntity.defDpo;
        this.defPr = targetEntity.defPr;
        this.defMr = targetEntity.defMr;
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

    public void calculateDefenseAndShips() {
        if (unknownDefense) {
            defenseSum = null;
        } else {
            defenseSum =
                    (long) (defWr * (2000)) +
                    (long) (defLdl * (1500 + 750)) +
                    (long) (defCdl * (6000 + 3000)) +
                    (long) (defDg * (20000 + 22500 + 6000)) +
                    (long) (defDj * (2000 + 9000)) +
                    (long) (defWp * (50000 + 75000 + 90000)) +
                    (long) (defMpo * (10000 + 15000)) +
                    (long) (defDpo * (50000 + 75000));
        }

        if(unknownFleet) {
            fleetSum = null;
        } else {
            fleetSum =
                    (long) (lm * (3000 + 1500)) +
                    (long) (cm * (6000 + 3000)) +
                    (long) (kr * (20000 + 10500 + 6000)) +
                    (long) (ow * (45000 + 22500)) +
                    (long) (pan * (30000 + 60000 + 45000)) +
                    (long) (bom * (50000 + 37500 + 45000)) +
                    (long) (ni * (60000 + 75000 + 45000)) +
                    (long) (gs * (5000000 + 6000000 + 3000000)) +
                    (long) (mt * (50000 + 75000)) +
                    (long) (dt * (50000 + 75000)) +
                    (long) (kol * (50000 + 75000)) +
                    (long) (rec * (50000 + 75000)) +
                    (long) (son * (50000 + 75000)) +
                    (long) (sat * (50000 + 75000));
        }
    }
}
