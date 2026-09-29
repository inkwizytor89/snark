package org.enoch.snark.instance.model.exception;

import org.enoch.snark.db.entity.ColonyEntity;

public class FleetIsCurrentlyInCombatException extends LogicException {
    public FleetIsCurrentlyInCombatException(ColonyEntity colony) {
        super(colony+": The fleet is currently in combat.");
    }
}
