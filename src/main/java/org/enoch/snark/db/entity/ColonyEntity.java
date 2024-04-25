package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.gi.types.ShipEnum;
import org.enoch.snark.instance.model.to.Resources;
import org.enoch.snark.instance.model.to.ShipsMap;
import org.enoch.snark.instance.model.to.SystemView;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.*;

import static org.enoch.snark.instance.model.to.ShipsMap.ALL_SHIPS;
import static org.enoch.snark.instance.model.to.ShipsMap.NO_SHIPS;

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
    @Column(name = "forms")
    public String forms;

    @Basic
    @Column(name = "level")
    public Long level;

    @Basic
    @Column(name = "forms_level")
    public Long formsLevel;

    public Collection<SystemView> generateSystemToView() {

        List<SystemView> result = new ArrayList<>();
        // TODO: extend about begin-end circle
        int begin = system - 5;
        int end = system + 5;
        for(int i = begin; i < end; i++ ) {
            result.add(new SystemView(galaxy, i));
        }
        return result;
    }

    public boolean hasEnoughResources(Resources required) {
        return getResources().isMoreThan(required);
    }

    public boolean hasEnoughShips(ShipsMap required) {
        if(required == null || ALL_SHIPS.equals(required) || NO_SHIPS.equals(required)) return true;
        ShipsMap located = this.getShipsMap();
        for(Map.Entry<ShipEnum, Long> entry : required.entrySet()) {
            Long requiredCont = required.get(entry.getKey());
            if(requiredCont.equals(Long.MAX_VALUE)) continue;
            Long locatedCount = located.get(entry.getKey());
            if(locatedCount < requiredCont) {
                return false;
            }
        }
        return true;
    }

    public boolean hasEnoughTransporters() {
        return calculateTransportByTransporterSmall() < transporterSmall + 5 * transporterLarge;
    }

    public ColonyEntity save() {
        ColonyDAO.getInstance().saveOrUpdate(this);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColonyEntity that = (ColonyEntity) o;
        return Objects.equals(cp, that.cp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cp);
    }
}
