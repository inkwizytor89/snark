package org.enoch.snark.instance.model.action.condition;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.FleetPromise;

import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class SpyNotExpiredCondition extends AbstractCondition {
    private final Long seconds;

    public SpyNotExpiredCondition(Long seconds) {
        this.seconds = seconds;
    }

    @Override
    public boolean fit(FleetPromise promise) {
        Optional<TargetEntity> targetEntityOptional = TargetDAO.getInstance().find(promise.getTarget());
        if(targetEntityOptional.isEmpty()) throw new IllegalStateException(promise.getTarget() + " can not find in database");
        return !DateUtil.isExpired(targetEntityOptional.get().lastSpiedOn, seconds, ChronoUnit.SECONDS);
    }

    @Override
    public String reason(FleetPromise promise) {
        if(!fit(promise)) return promise.getTarget() + " have not expired lastSpiedOn in seconds " + seconds;
        else return MISSING_REASON;
    }
}
