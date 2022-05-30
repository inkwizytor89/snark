package org.enoch.snark.db.entity;

import org.enoch.snark.model.exception.TargetMissingResourceInfoException;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "targets", schema = "public", catalog = "snark")
public class TargetEntity extends BaseEntity {

    public final static String ADMIN = "ADMIN";
    public final static String NORMAL = "NORMAL";
    public final static String ABSENCE = "ABSENCE";
    public final static String WEAK = "WEAK";
    public final static String IN_ACTIVE = "IN_ACTIVE";

    @ManyToOne
    @JoinColumn(name = "planet_id", referencedColumnName = "id", nullable = false)
    public PlanetEntity planet;

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
        this.planet = new PlanetEntity(input);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TargetEntity entity = (TargetEntity) o;
        return Objects.equals(id, entity.id) &&
                Objects.equals(planet.galaxy, entity.planet.galaxy) &&
                Objects.equals(planet.system, entity.planet.system) &&
                Objects.equals(planet.position, entity.planet.position) &&
                Objects.equals(planet.power, entity.planet.power) &&
                Objects.equals(type, entity.type) &&
                Objects.equals(fleetSum, entity.fleetSum) &&
                Objects.equals(defenseSum, entity.defenseSum) &&
                Objects.equals(updated, entity.updated);
    }

    public void update(TargetEntity targetEntity) {
        this.planet.metal = targetEntity.planet.metal;
        this.planet.crystal = targetEntity.planet.crystal;
        this.planet.deuterium = targetEntity.planet.deuterium;
        this.resources = 3 * planet.metal + 2 * planet.crystal + planet.deuterium;
        this.planet.power = targetEntity.planet.power;

        this.fleetSum = targetEntity.fleetSum;
        this.defenseSum = targetEntity.defenseSum;

        this.planet.lm = targetEntity.planet.lm;
        this.planet.cm = targetEntity.planet.cm;
        this.planet.kr = targetEntity.planet.kr;
        this.planet.ow = targetEntity.planet.ow;
        this.planet.pan = targetEntity.planet.pan;
        this.planet.bom = targetEntity.planet.bom;
        this.planet.ni = targetEntity.planet.ni;
        this.planet.gs = targetEntity.planet.gs;
        this.planet.mt = targetEntity.planet.mt;
        this.planet.dt = targetEntity.planet.dt;
        this.planet.kol = targetEntity.planet.kol;
        this.planet.rec = targetEntity.planet.rec;
        this.planet.son = targetEntity.planet.son;
        this.planet.sat = targetEntity.planet.sat;

        this.planet.wr = targetEntity.planet.wr;
        this.planet.ldl = targetEntity.planet.ldl;
        this.planet.cdl = targetEntity.planet.cdl;
        this.planet.dg = targetEntity.planet.dg;
        this.planet.dj = targetEntity.planet.dj;
        this.planet.wp = targetEntity.planet.wp;
        this.planet.mpo = targetEntity.planet.mpo;
        this.planet.dpo = targetEntity.planet.dpo;
        this.planet.pr = targetEntity.planet.pr;
        this.planet.mr = targetEntity.planet.mr;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, this.planet.galaxy, this.planet.system, this.planet.position, this.planet.power, type, fleetSum, defenseSum, updated);
    }

    public Long calculateTransportByLt() {
        if (this.planet.metal == null || this.planet.crystal == null || this.planet.deuterium == null) {
            throw new TargetMissingResourceInfoException();
        }
        return (long) Math.ceil((double) (this.planet.metal+this.planet.crystal+this.planet.deuterium)/10000);
    }
// todo zrobic zeby zmienna przy przemnazaniu sie nie przekrecila
    public void calculateDefenseAndShips() {
        if (this.planet.wr != null) {
            defenseSum =
                    (long) (this.planet.wr * (2000)) +
                    (long) (this.planet.ldl * (1500 + 750)) +
                    (long) (this.planet.cdl * (6000 + 3000)) +
                    (long) (this.planet.dg * (20000 + 22500 + 6000)) +
                    (long) (this.planet.dj * (2000 + 9000)) +
                    (long) (this.planet.wp * (50000 + 75000 + 90000)) +
                    (long) (this.planet.mpo * (10000 + 15000)) +
                    (long) (this.planet.dpo * (50000 + 75000));
        }
        if(this.planet.lm != null) {
            fleetSum =
                    (long) (this.planet.lm * (3000 + 1500)) +
                    (long) (this.planet.cm * (6000 + 3000)) +
                    (long) (this.planet.kr * (20000 + 10500 + 6000)) +
                    (long) (this.planet.ow * (45000 + 22500)) +
                    (long) (this.planet.pan * (30000 + 60000 + 45000)) +
                    (long) (this.planet.bom * (50000 + 37500 + 45000)) +
                    (long) (this.planet.ni * (60000 + 75000 + 45000)) +
                    (long) (this.planet.gs * (5000000 + 6000000 + 3000000)) +
                    (long) (this.planet.mt * (50000 + 75000)) +
                    (long) (this.planet.dt * (50000 + 75000)) +
                    (long) (this.planet.kol * (50000 + 75000)) +
                    (long) (this.planet.rec * (50000 + 75000)) +
                    (long) (this.planet.son * (50000 + 75000)) +
                    (long) (this.planet.sat * (50000 + 75000));
        }
    }
}
