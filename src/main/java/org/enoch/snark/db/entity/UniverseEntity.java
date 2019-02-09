package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.util.Collection;
import java.util.Objects;

@Entity
@Table(name = "universes")
public class UniverseEntity {
    private Long id;
    private String login;
    private String pass;
    private String name;
    private String tag;
    private String url;
    private Integer galaxyMax;
    private Integer systemMax;
    private Integer explorationArea;
    private Collection<GalaxyEntity> galaxiesById;
    private Collection<PlanetEntity> planetsById;
    private Collection<SourceEntity> sources;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "login")
    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    @Basic
    @Column(name = "pass")
    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "tag")
    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    @Basic
    @Column(name = "url")
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Basic
    @Column(name = "galaxy_max")
    public Integer getGalaxyMax() {
        return galaxyMax;
    }

    public void setGalaxyMax(Integer galaxyCount) {
        this.galaxyMax = galaxyCount;
    }

    @Basic
    @Column(name = "system_max")
    public Integer getSystemMax() {
        return systemMax;
    }

    public void setSystemMax(Integer systemMax) {
        this.systemMax = systemMax;
    }

    @Basic
    @Column(name = "exploration_area")
    public Integer getExplorationArea() {
        return explorationArea;
    }

    public void setExplorationArea(Integer explorationArea) {
        this.explorationArea = explorationArea;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UniverseEntity that = (UniverseEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(login, that.login) &&
                Objects.equals(pass, that.pass) &&
                Objects.equals(name, that.name) &&
                Objects.equals(tag, that.tag) &&
                Objects.equals(url, that.url) &&
                Objects.equals(galaxyMax, that.galaxyMax) &&
                Objects.equals(explorationArea, that.explorationArea);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, login, pass, name, tag, url, galaxyMax, explorationArea);
    }

//    @OneToMany(mappedBy = "universesByUniversId")
//    public Collection<GalaxyEntity> getGalaxiesById() {
//        return galaxiesById;
//    }

    public void setGalaxiesById(Collection<GalaxyEntity> galaxiesById) {
        this.galaxiesById = galaxiesById;
    }

//    @OneToMany(mappedBy = "universesByUniverseId")
//    public Collection<PlanetEntity> getPlanetsById() {
//        return planetsById;
//    }

    public void setPlanetsById(Collection<PlanetEntity> planetsById) {
        this.planetsById = planetsById;
    }

//    @OneToMany(mappedBy = "universesByUniverseId")
//    public Collection<SourceEntity> getSourcesById() {
//        return sources;
//    }

    @OneToMany(mappedBy = "universes")
    public Collection<SourceEntity> getSources() {
        return sources;
    }

    public void setSources(Collection<SourceEntity> sources) {
        this.sources = sources;
    }
}
