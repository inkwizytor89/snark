package org.enoch.snark.instance.model.action.promisecondition;

import org.enoch.snark.db.entity.TargetEntity;

import java.time.LocalDateTime;

public class SpyNotExpiredPromiseCondition extends ExpiredPromiseCondition {

    protected SpyNotExpiredPromiseCondition(Long seconds, Boolean is) {
        super(seconds, is);
    }

    @Override
    protected LocalDateTime getDate(TargetEntity targetEntity) {
        return targetEntity.lastSpiedOn;
    }

    @Override
    protected String getType() {
        return SpyNotExpiredPromiseCondition.class.getSimpleName();
    }
}
