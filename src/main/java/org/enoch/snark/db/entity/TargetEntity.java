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
    public Integer spyLevel = 4;

    @Basic
    @Column(name = "updated")
    public LocalDateTime updated;

    @Basic
    @Column(name = "last_attacked")
    public LocalDateTime lastAttacked;

    @ManyToOne
    @JoinColumn(name = "player_id", referencedColumnName = "id", nullable = false)
    public PlayerEntity player;

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
                Objects.equals(energy, entity.energy) &&
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
        this.energy = targetEntity.energy;

        this.fleetSum = targetEntity.fleetSum;
        this.defenseSum = targetEntity.defenseSum;

        this.fighterLight = targetEntity.fighterLight;
        this.fighterHeavy = targetEntity.fighterHeavy;
        this.cruiser = targetEntity.cruiser;
        this.battleship = targetEntity.battleship;
        this.interceptor = targetEntity.interceptor;
        this.bomber = targetEntity.bomber;
        this.destroyer = targetEntity.destroyer;
        this.deathstar = targetEntity.deathstar;
        this.transporterSmall = targetEntity.transporterSmall;
        this.transporterLarge = targetEntity.transporterLarge;
        this.colonyShip = targetEntity.colonyShip;
        this.recycler = targetEntity.recycler;
        this.espionageProbe = targetEntity.espionageProbe;
        this.sat = targetEntity.sat;

        this.rocketLauncher = targetEntity.rocketLauncher;
        this.laserCannonLight = targetEntity.laserCannonLight;
        this.laserCannonHeavy = targetEntity.laserCannonHeavy;
        this.gaussCannon = targetEntity.gaussCannon;
        this.ionCannon = targetEntity.ionCannon;
        this.plasmaCannon = targetEntity.plasmaCannon;
        this.shieldDomeSmall = targetEntity.shieldDomeSmall;
        this.shieldDomeLarge = targetEntity.shieldDomeLarge;
        this.missileInterceptor = targetEntity.missileInterceptor;
        this.missileInterplanetary = targetEntity.missileInterplanetary;

        this.spyLevel = targetEntity.spyLevel;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, this.galaxy, this.system, this.position, this.energy, type, fleetSum, defenseSum, updated);
    }

    public Long calculateTransportByLt() {
        if (this.metal == null || this.crystal == null || this.deuterium == null) {
            throw new TargetMissingResourceInfoException();
        }
        return (long) Math.ceil((double) (this.metal+this.crystal+this.deuterium)/10000);
    }
// todo zrobic zeby zmienna przy przemnazaniu sie nie przekrecila
    public void calculateDefenseAndShips() {
        if (this.rocketLauncher != null) {
            defenseSum =
                    (long) (this.rocketLauncher * (2000)) +
                    (long) (this.laserCannonLight * (1500 + 750)) +
                    (long) (this.laserCannonHeavy * (6000 + 3000)) +
                    (long) (this.gaussCannon * (20000 + 22500 + 6000)) +
                    (long) (this.ionCannon * (2000 + 9000)) +
                    (long) (this.plasmaCannon * (50000 + 75000 + 90000)) +
                    (long) (this.shieldDomeSmall * (10000 + 15000)) +
                    (long) (this.shieldDomeLarge * (50000 + 75000));
        }
        if(this.fighterLight != null) {
            fleetSum =
                    (long) (this.fighterLight * (3000 + 1500)) +
                    (long) (this.fighterHeavy * (6000 + 3000)) +
                    (long) (this.cruiser * (20000 + 10500 + 6000)) +
                    (long) (this.battleship * (45000 + 22500)) +
                    (long) (this.interceptor * (30000 + 60000 + 45000)) +
                    (long) (this.bomber * (50000 + 37500 + 45000)) +
                    (long) (this.destroyer * (60000 + 75000 + 45000)) +
                    (long) (this.deathstar * (5000000 + 6000000 + 3000000)) +
                    (long) (this.transporterSmall * (50000 + 75000)) +
                    (long) (this.transporterLarge * (50000 + 75000)) +
                    (long) (this.colonyShip * (50000 + 75000)) +
                    (long) (this.recycler * (50000 + 75000)) +
                    (long) (this.espionageProbe * (50000 + 75000)) +
                    (long) (this.sat * (50000 + 75000));
        }
    }
}
