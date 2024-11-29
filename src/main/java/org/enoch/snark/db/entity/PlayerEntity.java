package org.enoch.snark.db.entity;

import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.db.dao.PlayerDAO;
import org.enoch.snark.instance.model.to.HighScorePosition;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
    @Column(name = "all_points")
    public Long allPoints;

    @Basic
    @Column(name = "economy_points")
    public Long economyPoints;

    @Basic
    @Column(name = "research_points")
    public Long researchPoints;

    @Basic
    @Column(name = "fleet_points")
    public Long fleetPoints;

    @Basic
    @Column(name = "ships_count")
    public Long shipsCount;

    @Basic
    @Column(name = "lifeform_points")
    public Long lifeformPoints;

    @Basic
    @Column(name = "tags")
    public String tags;

    @Basic
    @Column(name = "spy_level")
    public Long spyLevel = 4L;

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

    public void update(HighScorePosition highScorePosition) {
        if(!StringUtils.isEmpty(highScorePosition.name)) this.name = highScorePosition.name;
        if(highScorePosition.points != null) this.allPoints = highScorePosition.points;
        if(highScorePosition.economy != null) {
            if(highScorePosition.economy < this.economyPoints) System.err.println("\neconomy-decrease for "+highScorePosition.code+" "+
                    highScorePosition.name+" "+this.economyPoints+ "->"+highScorePosition.economy);
            this.economyPoints = highScorePosition.economy;
        }
        if(highScorePosition.fleet != null) {
            if(highScorePosition.fleet < this.fleetPoints) System.err.println("\nfleet-decrease for "+highScorePosition.code+" "+
                    highScorePosition.name+" "+this.fleetPoints+ "->"+highScorePosition.fleet);
            this.fleetPoints = highScorePosition.fleet;
        }
        if(highScorePosition.ships != null) {
            if(highScorePosition.ships < this.shipsCount) System.err.println("\nships-decrease for "+highScorePosition.code+" "+
                    highScorePosition.name+" "+this.shipsCount+ "->"+highScorePosition.ships);
            this.shipsCount = highScorePosition.ships;
        }
        this.updated = LocalDateTime.now();
        PlayerDAO.getInstance().saveOrUpdate(this);
    }
}
