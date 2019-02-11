package org.enoch.snark.db.entity;

import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sources", schema = "public", catalog = "snark")
public class SourceEntity {
    private Long id;
    private Integer galaxy;
    private Integer system;
    private Integer position;
    private Integer cp;
    private UniverseEntity universe;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "galaxy")
    public Integer getGalaxy() {
        return galaxy;
    }

    public void setGalaxy(Integer galaxy) {
        this.galaxy = galaxy;
    }

    @Basic
    @Column(name = "system")
    public Integer getSystem() {
        return system;
    }

    public void setSystem(Integer system) {
        this.system = system;
    }

    @Basic
    @Column(name = "position")
    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Basic
    @Column(name = "cp")
    public Integer getCp() {
        return cp;
    }

    public void setCp(Integer cp) {
        this.cp = cp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceEntity that = (SourceEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(galaxy, that.galaxy) &&
                Objects.equals(system, that.system) &&
                Objects.equals(position, that.position) &&
                Objects.equals(cp, that.cp);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, galaxy, system, position, cp);
    }

    @ManyToOne
    @JoinColumn(name = "universe_id", referencedColumnName = "id", nullable = false)
    public UniverseEntity getUniverse() {
        return universe;
    }

    public void setUniverse(UniverseEntity universe) {
        this.universe = universe;
    }


    public Collection<SystemView> generateSystemToView() {

        List<SystemView> result = new ArrayList<>();
        // TODO: extend about begin-end circle
        int begin = system - universe.getExplorationArea();
        int end = system + universe.getExplorationArea();
        for(int i = begin; i < end; i++ ) {
            result.add(new SystemView(galaxy, i));
        }
        return result;
    }
}
