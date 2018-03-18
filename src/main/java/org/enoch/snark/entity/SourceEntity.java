package org.enoch.snark.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "sources", schema = "public", catalog = "snark")
public class SourceEntity {
    private long id;
    private int galaxy;
    private int system;
    private int position;
    private int cp;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "galaxy")
    public int getGalaxy() {
        return galaxy;
    }

    public void setGalaxy(int galaxy) {
        this.galaxy = galaxy;
    }

    @Basic
    @Column(name = "system")
    public int getSystem() {
        return system;
    }

    public void setSystem(int system) {
        this.system = system;
    }

    @Basic
    @Column(name = "position")
    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Basic
    @Column(name = "cp")
    public int getCp() {
        return cp;
    }

    public void setCp(int cp) {
        this.cp = cp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceEntity that = (SourceEntity) o;
        return id == that.id &&
                galaxy == that.galaxy &&
                system == that.system &&
                position == that.position &&
                cp == that.cp;
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, galaxy, system, position, cp);
    }
}
