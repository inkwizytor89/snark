package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;

@Entity
@Table(name = "farm_waves", schema = "public", catalog = "snark")
public class FarmEntity extends BaseEntity {

    @Basic
    @Column(name = "start")
    public LocalDateTime start;

    public Long spyRequestCode;

    public Long warRequestCode;


}
