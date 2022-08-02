package org.enoch.snark.db.entity;

import org.enoch.snark.model.Planet;
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
    @Column(name = "resources")
    public Long resources;

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

        this.wr = targetEntity.wr;
        this.ldl = targetEntity.ldl;
        this.cdl = targetEntity.cdl;
        this.dg = targetEntity.dg;
        this.dj = targetEntity.dj;
        this.wp = targetEntity.wp;
        this.mpo = targetEntity.mpo;
        this.dpo = targetEntity.dpo;
        this.pr = targetEntity.pr;
        this.mr = targetEntity.mr;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, this.galaxy, this.system, this.position, this.power, type, fleetSum, defenseSum, updated);
    }

    public Long calculateTransportByLt() {
        if (this.metal == null || this.crystal == null || this.deuterium == null) {
            throw new TargetMissingResourceInfoException();
        }
        return (long) Math.ceil((double) (this.metal+this.crystal+this.deuterium)/10000);
    }
// todo zrobic zeby zmienna przy przemnazaniu sie nie przekrecila
    public void calculateDefenseAndShips() {
        if (this.wr != null) {
            defenseSum =
                    (long) (this.wr * (2000)) +
                    (long) (this.ldl * (1500 + 750)) +
                    (long) (this.cdl * (6000 + 3000)) +
                    (long) (this.dg * (20000 + 22500 + 6000)) +
                    (long) (this.dj * (2000 + 9000)) +
                    (long) (this.wp * (50000 + 75000 + 90000)) +
                    (long) (this.mpo * (10000 + 15000)) +
                    (long) (this.dpo * (50000 + 75000));
        }
        if(this.lm != null) {
            fleetSum =
                    (long) (this.lm * (3000 + 1500)) +
                    (long) (this.cm * (6000 + 3000)) +
                    (long) (this.kr * (20000 + 10500 + 6000)) +
                    (long) (this.ow * (45000 + 22500)) +
                    (long) (this.pan * (30000 + 60000 + 45000)) +
                    (long) (this.bom * (50000 + 37500 + 45000)) +
                    (long) (this.ni * (60000 + 75000 + 45000)) +
                    (long) (this.gs * (5000000 + 6000000 + 3000000)) +
                    (long) (this.mt * (50000 + 75000)) +
                    (long) (this.dt * (50000 + 75000)) +
                    (long) (this.kol * (50000 + 75000)) +
                    (long) (this.rec * (50000 + 75000)) +
                    (long) (this.son * (50000 + 75000)) +
                    (long) (this.sat * (50000 + 75000));
        }
    }
}
