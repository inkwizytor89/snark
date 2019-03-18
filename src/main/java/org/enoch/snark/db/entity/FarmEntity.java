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

    @Basic
    @Column(name = "spy_requests_code")
    public Long spyRequestCode;

    @Basic
    @Column(name = "war_requests_code")
    public Long warRequestCode;

}
