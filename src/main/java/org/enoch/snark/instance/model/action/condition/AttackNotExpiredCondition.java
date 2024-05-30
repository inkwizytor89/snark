package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.TargetEntity;

import java.time.LocalDateTime;

public class AttackNotExpiredCondition extends ExpiredCondition {

    protected AttackNotExpiredCondition(Long seconds, Boolean is) {
        super(seconds, is);
    }

    @Override
    protected LocalDateTime getDate(TargetEntity targetEntity) {
        return targetEntity.lastAttacked;
    }

    @Override
    protected String getType() {
        return AttackNotExpiredCondition.class.getSimpleName();
    }
}
