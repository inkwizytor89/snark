// ================================
// 1. FUNCTION DEFINITIONS
// ================================

package org.enoch.snark.expression.definition;

import java.util.List;

public record SpelFunctionDefinition(
        String name,
        String description,
        List<SpelParameterDefinition> parameters,
        SpellType returnType
) {
}