package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "planets", schema = "public", catalog = "snark")
public class SpyRequestEntity {

    private Long id;
    private UniverseEntity universe;
    private PlanetEntity planet;
    private Timestamp visited;
    private Timestamp back;


    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id",unique=true, nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne
    @JoinColumn(name = "universe_id", referencedColumnName = "id", nullable = false)
    public UniverseEntity getUniverse() {
        return universe;
    }

    public void setUniverse(UniverseEntity universe) {
        this.universe = universe;
    }

    @ManyToOne
    @JoinColumn(name = "planet_id", referencedColumnName = "id", nullable = false)
    public PlanetEntity getPlanet() {
        return planet;
    }

    public void setPlanet(PlanetEntity planet) {
        this.planet = planet;
    }

    @Basic
    @Column(name = "visited")
    public Timestamp getVisited() {
        return visited;
    }

    public void setVisited(Timestamp visited) {
        this.visited = visited;
    }

    @Basic
    @Column(name = "back")
    public Timestamp getBack() {
        return back;
    }

    public void setBack(Timestamp back) {
        this.back = back;
    }


}
