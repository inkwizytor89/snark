package org.enoch.snark.instance.model.to;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
import lombok.Getter;
import org.enoch.snark.db.entity.ColonyEntity;
import org.enoch.snark.instance.model.types.ColonyType;

@Data
public class Target {

    private Integer galaxy;
    private Integer system;
    private Integer position;
    private ColonyType type = ColonyType.PLANET;
}
