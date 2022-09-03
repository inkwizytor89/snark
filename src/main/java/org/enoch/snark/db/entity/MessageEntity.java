package org.enoch.snark.db.entity;

import org.enoch.snark.model.Planet;
import org.enoch.snark.model.SpyInfoParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name = "messages", schema = "public", catalog = "snark")
public class MessageEntity extends IdEntity {

    public static final String SPY = "SPY";

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

    public static MessageEntity create(String content) {
        MessageEntity messageEntity = new MessageEntity();
        messageEntity.content = content;
        messageEntity.type = SPY;
        // TODO: 12.03.2019 ustawic wartosc z raportu zamiast obecnej daty
        messageEntity.created = LocalDateTime.now();
        final String[] reportLines = content.split("\\R+");
//        messageEntity.messageId = Long.parseLong(reportLines[0]);
        return messageEntity;
    }

    public TargetEntity getPlanet() {
        return new SpyInfoParser(content).extractPlanetEntity();
    }
}
