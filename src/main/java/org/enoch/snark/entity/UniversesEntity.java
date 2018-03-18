package org.enoch.snark.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "universes", schema = "public", catalog = "snark")
public class UniversesEntity {
    private long id;
    private String login;
    private String pass;
    private String name;
    private String tag;
    private String url;
    private Integer galaxyCount;
    private Integer explorationArea;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
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
    @Column(name = "galaxy_count")
    public Integer getGalaxyCount() {
        return galaxyCount;
    }

    public void setGalaxyCount(Integer galaxyCount) {
        this.galaxyCount = galaxyCount;
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
        UniversesEntity that = (UniversesEntity) o;
        return id == that.id &&
                Objects.equals(login, that.login) &&
                Objects.equals(pass, that.pass) &&
                Objects.equals(name, that.name) &&
                Objects.equals(tag, that.tag) &&
                Objects.equals(url, that.url) &&
                Objects.equals(galaxyCount, that.galaxyCount) &&
                Objects.equals(explorationArea, that.explorationArea);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, login, pass, name, tag, url, galaxyCount, explorationArea);
    }
}
