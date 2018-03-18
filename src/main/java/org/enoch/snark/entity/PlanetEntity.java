package org.enoch.snark.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "planets", schema = "public", catalog = "snark")
public class PlanetEntity {
    private long id;
    private int galaxy;
    private int system;
    private int position;
    private int power;
    private String type;

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
    @Column(name = "power")
    public int getPower() {
        return power;
    }

    public void setPower(int power) {
        this.power = power;
    }

    @Basic
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlanetEntity that = (PlanetEntity) o;
        return id == that.id &&
                galaxy == that.galaxy &&
                system == that.system &&
                position == that.position &&
                power == that.power &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, galaxy, system, position, power, type);
    }
}
