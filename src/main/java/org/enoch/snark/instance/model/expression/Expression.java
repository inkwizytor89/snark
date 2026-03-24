package org.enoch.snark.instance.model.expression;


public class Expression  {

    private String expression;

    public Expression(String value, ExpressionContext context) {
        expression = value;
        expression = expression.replaceAll("#source", "\""+context.getSourceString()+"\"");
    }

    public Expression(String value) {
        if (value.contains("#source")) {
            throw new IllegalArgumentException("Expression cannot contain #source variable: " + value);
        }
       expression = value;
    }

     public String getValue() {
        return expression;
    }
}
