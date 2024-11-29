package org.enoch.snark.db.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
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
