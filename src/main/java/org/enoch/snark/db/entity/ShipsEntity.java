package org.enoch.snark.db.entity;

import javax.persistence.*;

@Entity
@Table(name = "ships", schema = "public", catalog = "snark")
public class ShipsEntity {

    @Id
    @Column(name = "id",unique=true, nullable = false)
    public Long id;

    @OneToOne
    @JoinColumn(name = "fleet_id")
    public FleetEntity fleet;

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
}
