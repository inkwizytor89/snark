package org.enoch.snark.instance.model.action.promisecondition;

import org.enoch.snark.db.entity.TargetEntity;

import java.time.LocalDateTime;

public class AttackNotExpiredPromiseCondition extends ExpiredPromiseCondition {

    protected AttackNotExpiredPromiseCondition(Long seconds, Boolean is) {
        super(seconds, is);
    }

    @Override
    protected LocalDateTime getDate(TargetEntity targetEntity) {
        return targetEntity.lastAttacked;
    }

    @Override
    protected String getType() {
        return AttackNotExpiredPromiseCondition.class.getSimpleName();
    }
}
