package org.enoch.snark.db.entity;

import org.enoch.snark.model.Planet;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name = "messages", schema = "public", catalog = "snark")
public class MessageEntity extends BaseEntity {

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

    public PlanetEntity getPlanet() {
        PlanetEntity planet = new PlanetEntity();
        final String[] reportLines = content.split("\\R+");
        for (int i = 1; i < reportLines.length; i++) {
            if(reportLines[i].equals("Raport szpiegowski z")) {
                final String reportInfo = reportLines[i + 1];
                final String[] split = reportInfo.split("\\s+");
                String time = split[split.length-1];
                String date = split[split.length-2];
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
                //planet.update = LocalDateTime.parse(date +" " + time, formatter);
                planet = new PlanetEntity(split[split.length-3]);
                i++;
            }
            if(reportLines[i].equals("Surowce")) {
                planet.metal = Long.parseLong(reportLines[i+1].replace(".", ""));
                planet.crystal = Long.parseLong(reportLines[i+2].replace(".", ""));
                planet.deuterium = Long.parseLong(reportLines[i+3].replace(".", ""));
//                resourcePoint = metal*2 + crystal*3 + deuterium*6;
                planet.power = Long.parseLong(reportLines[i+4].replace(".", ""));
                i+=4;
            }
        }
        return planet;
    }
}
