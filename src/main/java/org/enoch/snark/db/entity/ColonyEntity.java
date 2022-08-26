package org.enoch.snark.db.entity;

import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "colonies", schema = "public", catalog = "snark")
public class ColonyEntity extends PlanetEntity {

    @Basic
    @Column(name = "cp")
    public Integer cp;

    @Basic
    @Column(name = "cpm")
    public Integer cpm;

    @Basic
    @Column(name = "collecting_order")
    public Integer collectingOrder;

    public Collection<SystemView> generateSystemToView() {

        List<SystemView> result = new ArrayList<>();
        // TODO: extend about begin-end circle
        int begin = system - universe.explorationArea;
        int end = system + universe.explorationArea;
        for(int i = begin; i < end; i++ ) {
            result.add(new SystemView(galaxy, i));
        }
        return result;
    }

    public boolean canSent(FleetEntity fleet) {
        // todo change hardcoded logic on rea logic checking if is possible send fleet from colony
        return true;
    }
}
