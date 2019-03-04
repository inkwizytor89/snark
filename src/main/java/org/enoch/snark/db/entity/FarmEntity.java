package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;

@Entity
@Table(name = "farm_waves", schema = "public", catalog = "snark")
public class FarmEntity extends BaseEntity {

    @Basic
    @Column(name = "start")
    public Timestamp start;

    public Long spyRequestCode;

    public Long warRequestCode;


}
