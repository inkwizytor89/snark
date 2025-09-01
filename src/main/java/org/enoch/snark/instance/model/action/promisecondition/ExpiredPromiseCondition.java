package org.enoch.snark.instance.model.action.promisecondition;

import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.dao.TargetDAO;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.FleetPromise;
import org.enoch.snark.instance.model.to.Planet;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public abstract class ExpiredPromiseCondition extends AbstractPromiseCondition {
    private final Long seconds;
    private final Boolean is;

    protected ExpiredPromiseCondition(Long seconds, Boolean is) {
        this.seconds = seconds;
        this.is = is;
    }

    @Override
    public boolean fit(FleetPromise promise) {
        Optional<TargetEntity> targetEntityOptional = TargetDAO.getInstance().find(Planet.fromString(promise.getTarget()).getFirst());
        if(targetEntityOptional.isEmpty()) throw new IllegalStateException(promise.getTarget() + " can not find in database");
        if(getDate(targetEntityOptional.get())==null) return true;
        boolean isExpired = DateUtil.isExpired(getDate(targetEntityOptional.get()), seconds, ChronoUnit.SECONDS);
        return is == isExpired;
    }

    @Override
    public String reason(FleetPromise promise) {
        if(!fit(promise)) return promise.getTarget()+" should "+(!is?"not":"")+" expired "+" in seconds "+seconds
                + " for type " +getType();
        else return MISSING_REASON;
    }

    protected abstract LocalDateTime getDate(TargetEntity targetEntity);

    protected abstract String getType();
}
