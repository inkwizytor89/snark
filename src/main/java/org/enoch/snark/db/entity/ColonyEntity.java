package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.gi.macro.ShipEnum;
import org.enoch.snark.model.Resources;
import org.enoch.snark.model.SystemView;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

    public boolean hasEnoughShips(Map<ShipEnum, Long> required) {
        Map<ShipEnum, Long> located = this.getShipsMap();
        for(Map.Entry<ShipEnum, Long> entry : required.entrySet()) {
            Long requiredCont = required.get(entry.getKey());
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
}
