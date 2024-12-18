package org.enoch.snark.db.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "cache_entries", schema = "public", catalog = "snark")
public class CacheEntryEntity extends IdEntity {

    public static final String HIGH_SCORE = "HIGH_SCORE";

    @Basic
    @Column(name = "created")
    public LocalDateTime created = LocalDateTime.now();

    @Basic
    @Column(name = "key")
    public String key;

    @Basic
    @Column(name = "value")
    public String value;

    public Long getLong() {
        if(value == null) return null;
        return Long.parseLong(value);
    }

    public Integer getInt() {
        if(value == null) return null;
        return Integer.parseInt(value);
    }

    public void setLong(Long value) {
        if(value == null) this.value = null;
        else this.value = value.toString();
    }
}
