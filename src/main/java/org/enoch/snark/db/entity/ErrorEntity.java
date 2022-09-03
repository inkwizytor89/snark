package org.enoch.snark.db.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "errors", schema = "public", catalog = "snark")
public class ErrorEntity extends IdEntity {

    @Basic
    @Column(name = "message")
    public String message;

    @Basic
    @Column(name = "stack_trace")
    public String stackTrace;

    @Basic
    @Column(name = "created")
    public LocalDateTime created;

    @Basic
    @Column(name = "action")
    public String action;

    @Basic
    @Column(name = "value")
    public String value;

    @Basic
    @Column(name = "page")
    public String page;

}
