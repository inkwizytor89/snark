package org.enoch.snark.expression.function.list;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ListFunctionsTest {

    @Test
    void returnsFirstElement() {
        assertEquals("first", ListFunctions.first(List.of("first", "second")));
    }

    @Test
    void rejectsEmptyList() {
        assertThrows(IllegalArgumentException.class, () -> ListFunctions.first(List.of()));
    }

    @Test
    void rejectsNullList() {
        assertThrows(NullPointerException.class, () -> ListFunctions.first(null));
    }
}
