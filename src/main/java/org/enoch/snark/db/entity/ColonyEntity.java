package org.enoch.snark.db.entity;

import org.enoch.snark.db.dao.ColonyDAO;
import org.enoch.snark.instance.model.to.SystemView;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.*;

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
