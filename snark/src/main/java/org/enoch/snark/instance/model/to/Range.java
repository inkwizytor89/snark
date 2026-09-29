package org.enoch.snark.instance.model.to;

import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
public class Range<T extends Comparable<T>> {

    private final T from;
    private final T to;

    public Range(T from, T to) {
        this.from = from;
        this.to = to;
    }

    public static <T extends Comparable<T>> List<Range<T>> parse(
            String text, Function<String, T> converter) {

        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(text.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(part -> parseSingleRange(part, converter))
                .collect(Collectors.toList());
    }

    private static <T extends Comparable<T>> Range<T> parseSingleRange(String part, Function<String, T> converter) {
        String[] tokens = part.split("-");
        if (tokens.length == 1) {
            T val = converter.apply(tokens[0].trim());
            return new Range<>(val, val);
        } else if (tokens.length == 2) {
            T from = converter.apply(tokens[0].trim());
            T to = converter.apply(tokens[1].trim());
            return new Range<>(from, to);
        } else {
            throw new IllegalArgumentException("Invalid range format: " + part);
        }
    }

    /** Czy dany element mieści się w zakresie (włącznie) */
    public boolean contains(T value) {
        if (from == null || to == null || value == null) return false;
        return from.compareTo(value) <= 0 && value.compareTo(to) <= 0;
    }

    /** Czy zakres jest odwrócony (np. from > to) */
    public boolean isInverted() {
        if (from == null || to == null) return false;
        return from.compareTo(to) > 0;
    }

    /** Dla typów liczbowych – zwraca długość zakresu */
    public double sizeAsNumber() {
        if (from instanceof Number && to instanceof Number) {
            return ((Number) to).doubleValue() - ((Number) from).doubleValue();
        }
        throw new UnsupportedOperationException("sizeAsNumber działa tylko dla typów liczbowych");
    }

    @Override
    public String toString() {
        return "Range[" + from + " - " + to + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Range<?> range)) return false;
        return Objects.equals(from, range.from) && Objects.equals(to, range.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }
}
