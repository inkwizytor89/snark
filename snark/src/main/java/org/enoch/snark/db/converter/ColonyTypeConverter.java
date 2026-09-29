package org.enoch.snark.db.converter;

import org.enoch.snark.instance.model.types.ColonyType;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

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
