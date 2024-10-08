package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.db.entity.TargetEntity;

import java.time.LocalDateTime;

public class SpyNotExpiredCondition extends ExpiredCondition {

    protected SpyNotExpiredCondition(Long seconds, Boolean is) {
        super(seconds, is);
    }

    @Override
    protected LocalDateTime getDate(TargetEntity targetEntity) {
        return targetEntity.lastSpiedOn;
    }

    @Override
    protected String getType() {
        return SpyNotExpiredCondition.class.getSimpleName();
    }
}
