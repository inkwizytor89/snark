package org.enoch.snark.ai;


import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AstToSpelConverter {

    public String toSpel(AstExpression ast) {

        String args = ast.args()
                .values()
                .stream()
                .map(this::convertValue)
                .collect(Collectors.joining(", "));

        return "%s(%s)"
                .formatted(ast.function(), args);
    }

    private String convertValue(Object value) {

        if (value == null) {
            return "null";
        }

        // =========================
        // NESTED FUNCTION
        // =========================

        if (value instanceof Map<?, ?> map) {

            if (isAstExpression(map)) {

                AstExpression nested =
                        mapToAst(map);

                return toSpel(nested);
            }

            return convertMap(map);
        }

        // =========================
        // LIST
        // =========================

        if (value instanceof List<?> list) {

            return "{%s}".formatted(
                    list.stream()
                            .map(this::convertValue)
                            .collect(Collectors.joining(", "))
            );
        }

        // =========================
        // STRING
        // =========================

        if (value instanceof String str) {

            return "'%s'"
                    .formatted(
                            escape(str)
                    );
        }

        // =========================
        // BOOLEAN
        // =========================

        if (value instanceof Boolean bool) {

            return bool.toString();
        }

        // =========================
        // NUMBERS
        // =========================

        if (value instanceof Integer
                || value instanceof Long
                || value instanceof Double
                || value instanceof Float
                || value instanceof BigDecimal) {

            return value.toString();
        }

        throw new IllegalArgumentException(
                "Unsupported value type: "
                        + value.getClass()
        );
    }

    private String convertMap(Map<?, ?> map) {

        return "{%s}".formatted(

                map.entrySet()
                        .stream()
                        .map(entry ->

                                "'%s': %s"
                                        .formatted(
                                                entry.getKey(),
                                                convertValue(
                                                        entry.getValue()
                                                )
                                        )
                        )
                        .collect(Collectors.joining(", "))
        );
    }

    private boolean isAstExpression(
            Map<?, ?> map
    ) {

        return map.containsKey("function")
                && map.containsKey("args");
    }

    @SuppressWarnings("unchecked")
    private AstExpression mapToAst(
            Map<?, ?> map
    ) {

        return new AstExpression(
                (String) map.get("function"),
                (Map<String, Object>) map.get("args")
        );
    }

    private String escape(String value) {

        return value
                .replace("\\", "\\\\")
                .replace("'", "\\'");
    }
}