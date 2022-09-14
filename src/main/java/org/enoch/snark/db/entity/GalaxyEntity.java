package org.enoch.snark.db.entity;

import org.enoch.snark.model.SystemView;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "galaxy", schema = "public", catalog = "snark")
public class GalaxyEntity extends IdEntity /*implements Comparable<GalaxyEntity>*/ {

    @Basic
    @Column(name = "galaxy")
    public Integer galaxy;

    @Basic
    @Column(name = "system")
    public Integer system;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GalaxyEntity that = (GalaxyEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(galaxy, that.galaxy) &&
                Objects.equals(system, that.system) &&
                Objects.equals(updated, that.updated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, galaxy, system, updated);
    }

    @Override
    public String toString() {
        return "["+galaxy+", "+system+"]";
    }

    public SystemView toSystemView() {
        return new SystemView(galaxy, system);
    }

//    @Override
//    public int compareTo(GalaxyEntity o) {
//        return Integer.compare(getRanking(), otherPlayer.getRanking());
//    }

//    private int distance(GalaxyEntity entity) {
//        int result = 0;
//
//    }
}
