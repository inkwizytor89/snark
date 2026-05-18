// ================================
// 7. AST EXECUTOR
// ================================

package org.enoch.snark.expression;

import org.enoch.snark.expression.definition.AstExpression;
import org.enoch.snark.expression.function.AbstractSpelFunction;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class AstExecutor {

    private final ExpressionParser parser = new SpelExpressionParser();

    public Object execute(AstExpression ast) {
        StandardEvaluationContext context = new StandardEvaluationContext();
        context.setVariable("functions", new FunctionExecutor());

        String expression = buildExpression(ast);
        Expression exp = parser.parseExpression(expression);

        return exp.getValue(context);
    }

    public String buildExpression(AstExpression ast) {
        String functionName = ast.function();
        return "#functions." + functionName + "()";
    }

    public static class FunctionExecutor {
        public Object start() {
            AbstractSpelFunction func = AbstractSpelFunction.getFunction("start");
            return func != null ? func.execute(java.util.Map.of()) : "unknown";
        }

        public Object stop() {
            AbstractSpelFunction func = AbstractSpelFunction.getFunction("stop");
            return func != null ? func.execute(java.util.Map.of()) : "unknown";
        }

        public Object help() {
            AbstractSpelFunction func = AbstractSpelFunction.getFunction("help");
            return func != null ? func.execute(java.util.Map.of()) : "unknown";
        }

        public Object add(int a, int b) {
            AbstractSpelFunction func = AbstractSpelFunction.getFunction("add");
            return func != null ? func.execute(java.util.Map.of("a", a, "b", b)) : "unknown";
        }

        public Object concat(String left, String right) {
            AbstractSpelFunction func = AbstractSpelFunction.getFunction("concat");
            return func != null ? func.execute(java.util.Map.of("left", left, "right", right)) : "unknown";
        }

        public Object multiply(double a, double b) {
            AbstractSpelFunction func = AbstractSpelFunction.getFunction("multiply");
            return func != null ? func.execute(java.util.Map.of("a", a, "b", b)) : "unknown";
        }
    }
}