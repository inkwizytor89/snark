package org.enoch.snark.db.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "historic_targets", schema = "public", catalog = "snark")
public class HistoricTargetEntity extends IdEntity {

    @ManyToOne
    @JoinColumn(name = "target_id", referencedColumnName = "id", nullable = false)
    public TargetEntity target;

    @Basic
    @Column(name = "metal")
    public Long metal;

    @Basic
    @Column(name = "crystal")
    public Long crystal;

    @Basic
    @Column(name = "deuterium")
    public Long deuterium;

    @Basic
    @Column(name = "tags")
    public String tags;

    @Basic
    @Column(name = "resources")
    public Long resources;

    @Basic
    @Column(name = "fleet_sum")
    public Long fleetSum;

    @Basic
    @Column(name = "defense_sum")
    public Long defenseSum;

    @Basic
    @Column(name = "fighterLight")
    public Long fighterLight = 0L;

    @Basic
    @Column(name = "fighterHeavy")
    public Long fighterHeavy = 0L;

    @Basic
    @Column(name = "cruiser")
    public Long cruiser = 0L;

    @Basic
    @Column(name = "battleship")
    public Long battleship = 0L;

    @Basic
    @Column(name = "interceptor")
    public Long interceptor = 0L;

    @Basic
    @Column(name = "bomber")
    public Long bomber = 0L;

    @Basic
    @Column(name = "destroyer")
    public Long destroyer = 0L;

    @Basic
    @Column(name = "deathstar")
    public Long deathstar = 0L;

    @Basic
    @Column(name = "transporterSmall")
    public Long transporterSmall = 0L;

    @Basic
    @Column(name = "transporterLarge")
    public Long transporterLarge = 0L;

    @Basic
    @Column(name = "colonyShip")
    public Long colonyShip = 0L;

    @Basic
    @Column(name = "recycler")
    public Long recycler = 0L;

    @Basic
    @Column(name = "espionageProbe")
    public Long espionageProbe = 0L;

    @Basic
    @Column(name = "sat")
    public Long sat = 0L;

    @Basic
    @Column(name = "explorer")
    public Long explorer = 0L;

    @Basic
    @Column(name = "reaper")
    public Long reaper = 0L;

    @Basic
    @Column(name = "rocketLauncher")
    public Long rocketLauncher = 0L;

    @Basic
    @Column(name = "laserCannonLight")
    public Long laserCannonLight = 0L;

    @Basic
    @Column(name = "laserCannonHeavy")
    public Long laserCannonHeavy = 0L;

    @Basic
    @Column(name = "gaussCannon")
    public Long gaussCannon = 0L;

    @Basic
    @Column(name = "ionCannon")
    public Long ionCannon = 0L;

    @Basic
    @Column(name = "plasmaCannon")
    public Long plasmaCannon = 0L;

    @Basic
    @Column(name = "shieldDomeSmall")
    public Long shieldDomeSmall = 0L;

    @Basic
    @Column(name = "shieldDomeLarge")
    public Long shieldDomeLarge = 0L;

    @Basic
    @Column(name = "missileInterceptor")
    public Long missileInterceptor = 0L;

    @Basic
    @Column(name = "missileInterplanetary")
    public Long missileInterplanetary = 0L;

    public void update(HistoricTargetEntity targetEntity) {
        this.metal = targetEntity.metal;
        this.crystal = targetEntity.crystal;
        this.deuterium = targetEntity.deuterium;
        this.resources = metal + 2 * crystal + 3 * deuterium;

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

        this.updated = LocalDateTime.now();
    }

// todo zrobic zeby zmienna przy przemnazaniu sie nie przekrecila
    public void calculateShips() {
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
    public void calculateDefense() {
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
}
