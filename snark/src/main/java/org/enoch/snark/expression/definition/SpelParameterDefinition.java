package org.enoch.snark.expression.definition;

public record SpelParameterDefinition(
        String name,
        SpellType type,
        String description,
        boolean required
) {
}