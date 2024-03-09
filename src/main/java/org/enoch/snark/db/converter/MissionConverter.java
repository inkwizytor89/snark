package org.enoch.snark.db.converter;

import org.enoch.snark.gi.types.Mission;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class MissionConverter implements AttributeConverter<Mission, String> {
    @Override
    public String convertToDatabaseColumn(Mission mission) {
        return mission.name();
    }

    @Override
    public Mission convertToEntityAttribute(String s) {
        return Mission.convert(s);
    }
}
