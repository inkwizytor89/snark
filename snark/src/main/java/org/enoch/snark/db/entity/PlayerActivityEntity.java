package org.enoch.snark.db.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "players_activity", schema = "public", catalog = "snark")
public class PlayerActivityEntity extends IdEntity {

    @ManyToOne
    @JoinColumn(name = "player_id", referencedColumnName = "id", nullable = false)
    public PlayerEntity player;

    @Basic
    @Column(name = "counter")
    public Long counter;

    @Basic
    @Column(name = "tags")
    public String tags;
}
