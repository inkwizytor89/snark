package org.enoch.snark.instance.model.action.condition;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.enoch.snark.db.repository.ColonyRepository;
import org.enoch.snark.db.repository.FleetRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Data
public class ConditionContext {
    private final ColonyRepository colonyRepository;
    private final FleetRepository fleetRepository;
}
