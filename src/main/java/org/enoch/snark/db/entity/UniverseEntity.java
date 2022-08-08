package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "universes")
public class UniverseEntity extends IdEntity {

    @Basic
    @Column(name = "login")
    public String login;

    @Basic
    @Column(name = "pass")
    public String pass;

    @Basic
    @Column(name = "name")
    public String name;

    @Basic
    @Column(name = "mode")
    public String mode;

    @Basic
    @Column(name = "url")
    public String url;

    @Basic
    @Column(name = "galaxy_max")
    public Integer galaxyMax;

    @Basic
    @Column(name = "system_max")
    public Integer systemMax;

    @Basic
    @Column(name = "exploration_area")
    public Integer explorationArea;

    @OneToMany(mappedBy = "universe")
    public Collection<GalaxyEntity> galaxies;

    @OneToMany(mappedBy = "universe")
    public Collection<TargetEntity> targets;

    @OneToMany(mappedBy = "universe")
    public Collection<ColonyEntity> colonyEntities;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniverseEntity that = (UniverseEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(login, that.login) &&
                Objects.equals(pass, that.pass) &&
                Objects.equals(name, that.name) &&
                Objects.equals(mode, that.mode) &&
                Objects.equals(url, that.url) &&
                Objects.equals(galaxyMax, that.galaxyMax) &&
                Objects.equals(explorationArea, that.explorationArea);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, login, pass, name, mode, url, galaxyMax, explorationArea);
    }
}
