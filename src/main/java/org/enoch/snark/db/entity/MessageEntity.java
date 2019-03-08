package org.enoch.snark.db.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "messages", schema = "public", catalog = "snark")
public class MessageEntity extends BaseEntity {

    @Basic
    @Column(name = "created")
    public LocalDateTime created;

    @Basic
    @Column(name = "message_id")
    public Long messageId;

    @Basic
    @Column(name = "type")
    public String type;

    @Basic
    @Column(name = "content")
    public String content;

}
