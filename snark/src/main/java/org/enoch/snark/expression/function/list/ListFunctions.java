package org.enoch.snark.expression.function.list;

import java.util.List;
import java.util.Objects;

public final class ListFunctions {

    private ListFunctions() {
    }

    public static <T> T first(List<T> values) {
        Objects.requireNonNull(values, "values must not be null");
        if (values.isEmpty()) {
            throw new IllegalArgumentException("Cannot get the first element of an empty list");
        }
        return values.get(0);
    }
}
