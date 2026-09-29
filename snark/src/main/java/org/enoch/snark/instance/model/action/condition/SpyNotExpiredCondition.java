package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import org.enoch.snark.common.DateUtil;
import org.enoch.snark.db.entity.TargetEntity;
import org.enoch.snark.instance.model.to.Planet;

import java.time.temporal.ChronoUnit;

@Getter
public class SpyNotExpiredCondition extends ExpiredCondition {

    @JsonCreator
    public SpyNotExpiredCondition(
            @JsonProperty("seconds") Long seconds,
            @JsonProperty("is") Boolean is,
            @JsonProperty("target") Planet target
    ) {
        this.seconds = seconds;
        this.is = is;
        this.target = target;
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        TargetEntity target = context.target(this.target);
        boolean isExpired = DateUtil.isExpired(target.lastSpiedOn, seconds, ChronoUnit.SECONDS);
        boolean isPossible = is == isExpired;
        return isPossible ? null : this;
    }
}
