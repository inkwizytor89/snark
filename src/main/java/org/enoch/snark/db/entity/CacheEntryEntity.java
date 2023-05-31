package org.enoch.snark.db.entity;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "cache_entries", schema = "public", catalog = "snark")
public class CacheEntryEntity extends IdEntity {

    @Basic
    @Column(name = "created")
    public LocalDateTime created = LocalDateTime.now();

    @Basic
    @Column(name = "key")
    public Long key;

    @Basic
    @Column(name = "value")
    public Long value;
}
