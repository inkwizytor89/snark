package org.enoch.snark.db.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "players", schema = "public", catalog = "snark")
public class PlayerEntity extends IdEntity {

    @Basic
    @Column(name = "name")
    public String name;

    @Basic
    @Column(name = "code")
    public String code;

    @Basic
    @Column(name = "status")
    public String status;

    @Basic
    @Column(name = "type")
    public String type;

    @Basic
    @Column(name = "alliance")
    public String alliance;

    @Basic
    @Column(name = "level")
    public Long level;

//    Research
    @Basic
    @Column(name = "energyTechnology")
    public Long energyTechnology;

    @Basic
    @Column(name = "laserTechnology")
    public Long laserTechnology;

    @Basic
    @Column(name = "ionTechnology")
    public Long ionTechnology;

    @Basic
    @Column(name = "hyperspaceTechnology")
    public Long hyperspaceTechnology;

    @Basic
    @Column(name = "plasmaTechnology")
    public Long plasmaTechnology;

    @Basic
    @Column(name = "combustionDriveTechnology")
    public Long combustionDriveTechnology;

    @Basic
    @Column(name = "impulseDriveTechnology")
    public Long impulseDriveTechnology;

    @Basic
    @Column(name = "hyperspaceDriveTechnology")
    public Long hyperspaceDriveTechnology;

    @Basic
    @Column(name = "espionageTechnology")
    public Long espionageTechnology;

    @Basic
    @Column(name = "computerTechnology")
    public Long computerTechnology;

    @Basic
    @Column(name = "astrophysicsTechnology")
    public Long astrophysicsTechnology;

    @Basic
    @Column(name = "researchNetworkTechnology")
    public Long researchNetworkTechnology;

    @Basic
    @Column(name = "gravitonTechnology")
    public Long gravitonTechnology;

    @Basic
    @Column(name = "weaponsTechnology")
    public Long weaponsTechnology;

    @Basic
    @Column(name = "shieldingTechnology")
    public Long shieldingTechnology;

    @Basic
    @Column(name = "armorTechnology")
    public Long armorTechnology;

    public static PlayerEntity mainPlayer() {
        PlayerEntity player = new PlayerEntity();
        player.id = 1L;
        player.name = "";
        player.code = "";
        return player;
    }
}
