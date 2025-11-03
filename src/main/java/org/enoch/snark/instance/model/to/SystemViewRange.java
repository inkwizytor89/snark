package org.enoch.snark.instance.model.to;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class SystemViewRange {
    public Integer galaxy;
    public Range<Integer> systemRange;

    /**
     * Parses text like "6:123-155;5:222-255"
     * into a list of SystemViewRange objects.
     */
    public static List<SystemViewRange> parse(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(text.split(";"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(SystemViewRange::parseSingle)
                .collect(Collectors.toList());
    }

    private static SystemViewRange parseSingle(String part) {
        // Example part: "6:123-155"
        String[] tokens = part.split(":");
        if (tokens.length != 2) {
            throw new IllegalArgumentException("Invalid format, expected 'galaxy:from-to' but got: " + part);
        }

        try {
            Integer galaxy = Integer.parseInt(tokens[0].trim());
            String systemRangeText = tokens[1].trim();
            List<Range<Integer>> ranges = Range.parse(systemRangeText, Integer::parseInt);

            if (ranges.size() != 1) {
                throw new IllegalArgumentException("Expected exactly one range for part: " + part);
            }

            return new SystemViewRange(galaxy, ranges.get(0));

        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid galaxy number in: " + part, e);
        }
    }

}
