package org.enoch.snark.common;

import org.enoch.snark.instance.model.types.Expression;

public class NumberUtil {

    private static String normalizationString(String input) {
        if(input.contains("kk")) input = input.replaceAll("kk", "000000");
        if(input.contains("m")) input = input.replaceAll("m", "000000");
        if(input.contains("k")) input = input.replaceAll("k", "000");
        if(input.contains(".")) input = input.replaceAll("\\.", "");
        return input;
    }

    public static Long toLong(String input) {
        if(input.toLowerCase().contains(Expression.ALL.name().toLowerCase())) return Long.MAX_VALUE;
        return Long.parseLong(normalizationString(input));
    }

    public static Integer toInt(String input) {
        if(input.toLowerCase().contains(Expression.ALL.name().toLowerCase())) return Integer.MAX_VALUE;
        return Integer.parseInt(normalizationString(input));
    }
}
