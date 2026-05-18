// ================================
// 3. AST MODEL
// ================================

package org.enoch.snark.expression.definition;

import java.util.Map;

public record AstExpression(
        String function,
        Map<String, Object> args
) {
}