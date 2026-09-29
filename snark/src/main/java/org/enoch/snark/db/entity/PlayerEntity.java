package org.enoch.snark.db.entity;

import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.enoch.snark.instance.model.to.HighScorePosition;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Data
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
    @Column(name = "energy_technology")
    public Long energyTechnology;

    @Basic
    @Column(name = "laser_technology")
    public Long laserTechnology;

    @Basic
    @Column(name = "ion_technology")
    public Long ionTechnology;

    @Basic
    @Column(name = "hyperspace_technology")
    public Long hyperspaceTechnology;

    @Basic
    @Column(name = "plasma_technology")
    public Long plasmaTechnology;

    @Basic
    @Column(name = "combustion_drive_technology")
    public Long combustionDriveTechnology;

    @Basic
    @Column(name = "impulse_drive_technology")
    public Long impulseDriveTechnology;

    @Basic
    @Column(name = "hyperspace_drive_technology")
    public Long hyperspaceDriveTechnology;

    @Basic
    @Column(name = "espionage_technology")
    public Long espionageTechnology;

    @Basic
    @Column(name = "computer_technology")
    public Long computerTechnology;

    @Basic
    @Column(name = "astrophysics_technology")
    public Long astrophysicsTechnology;

    @Basic
    @Column(name = "research_network_technology")
    public Long researchNetworkTechnology;

    @Basic
    @Column(name = "graviton_technology")
    public Long gravitonTechnology;

    @Basic
    @Column(name = "weapons_technology")
    public Long weaponsTechnology;

    @Basic
    @Column(name = "shielding_technology")
    public Long shieldingTechnology;

    @Basic
    @Column(name = "armor_technology")
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
//        PlayerDAO.getInstance().saveOrUpdate(this);
    }
}
