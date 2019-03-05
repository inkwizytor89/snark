package org.enoch.snark.db.entity;

import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "sources", schema = "public", catalog = "snark")
public class SourceEntity extends PlanetEntity {

    @Basic
    @Column(name = "cp")
    public Integer cp;

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
