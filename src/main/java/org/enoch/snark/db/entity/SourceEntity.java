package org.enoch.snark.db.entity;

import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sources", schema = "public", catalog = "snark")
public class SourceEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "planet_id", referencedColumnName = "id", nullable = false)
    public PlanetEntity planet;

    @Basic
    @Column(name = "cp")
    public Integer cp;

    @Basic
    @Column(name = "collecting_order")
    public Integer collectingOrder;

    public Collection<SystemView> generateSystemToView() {

        List<SystemView> result = new ArrayList<>();
        // TODO: extend about begin-end circle
        int begin = planet.system - universe.explorationArea;
        int end = planet.system + universe.explorationArea;
        for(int i = begin; i < end; i++ ) {
            result.add(new SystemView(planet.galaxy, i));
        }
        return result;
    }
}
