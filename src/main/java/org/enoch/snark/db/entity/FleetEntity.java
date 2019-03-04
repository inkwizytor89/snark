package org.enoch.snark.db.entity;

import org.enoch.snark.model.Fleet;

import javax.annotation.Nonnull;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "fleet", schema = "public", catalog = "snark")
public class FleetEntity extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "planet_id", referencedColumnName = "id", nullable = false)
    public PlanetEntity planet;

    @Basic
    @Column(name = "start")
    public Timestamp start;

    @Basic
    @Column(name = "visited")
    public Timestamp visited;

    @Basic
    @Column(name = "back")
    public Timestamp back;

    @Column(name = "LM")
    public Long lm;

    @Column(name = "CM")
    public Long cm;

    @Column(name = "KR")
    public Long kr;

    @Column(name = "OW")
    public Long ow;

    @Column(name = "PAN")
    public Long pan;

    @Column(name = "BOM")
    public Long bom;

    @Column(name = "NI")
    public Long ni;

    @Column(name = "GS")
    public Long gs;

    @Column(name = "LT")
    public Long lt;

    @Column(name = "DT")
    public Long dt;

    @Column(name = "KOL")
    public Long kol;

    @Column(name = "REC")
    public Long rec;

    @Column(name = "SON")
    public Long son;

    FleetEntity() {}

    FleetEntity(@Nonnull Fleet fleet) {

    }
}
