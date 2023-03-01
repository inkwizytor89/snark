package org.enoch.snark.db.converter;

import org.enoch.snark.model.types.ColonyType;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter(autoApply = true)
public class ColonyTypeConverter implements AttributeConverter<ColonyType, String> {
    @Override
    public String convertToDatabaseColumn(ColonyType colonyType) {
        return colonyType.name();
    }

    @Override
    public ColonyType convertToEntityAttribute(String s) {
        return ColonyType.parse(s);
    }
}
