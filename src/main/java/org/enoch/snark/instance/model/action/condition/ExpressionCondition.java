package org.enoch.snark.instance.model.action.condition;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ExpressionCondition extends AbstractCondition {
    private final String term;
    private final String operator;
    private final String value;

    @JsonCreator
    public ExpressionCondition(
            @JsonProperty("term") String term,
            @JsonProperty("operator") String operator,
            @JsonProperty("value") String value
    ) {
        this.term = term;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public AbstractCondition check(ConditionContext context) {
        String cacheValue = context.getCacheValue(term);


        boolean isPossible;
        if("eq".equalsIgnoreCase(operator)) {
            isPossible = cacheValue.equalsIgnoreCase(value);
        } else  throw new IllegalStateException("Unknown operator "+operator);
        return isPossible ? null : this;
    }
}
