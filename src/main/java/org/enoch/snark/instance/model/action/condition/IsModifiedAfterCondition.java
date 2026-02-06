package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IsModifiedAfterCondition extends AbstractCondition {
    private final String firstTerm;
    private final String secondTerm;

    @JsonCreator
    public IsModifiedAfterCondition(
            @JsonProperty("firstTerm") String firstTerm,
            @JsonProperty("secondTerm") String secondTerm
    ) {
        this.firstTerm = firstTerm;
        this.secondTerm = secondTerm;
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        LocalDateTime firstTermUpdated = context.getCacheEntry(firstTerm).updated;
        LocalDateTime secondTermUpdated = context.getCacheEntry(secondTerm).updated;


        boolean isPossible = firstTermUpdated.isAfter(secondTermUpdated);
        return isPossible ? null : this;
    }
}
