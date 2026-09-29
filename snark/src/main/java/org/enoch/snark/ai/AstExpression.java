// ================================
// 3. AST MODEL
// ================================

package org.enoch.snark.ai;

import java.util.Map;

public record AstExpression(
        String function,
        Map<String, Object> args
) {
}