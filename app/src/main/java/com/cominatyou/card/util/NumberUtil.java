package com.cominatyou.card.util;

import java.util.Locale;

public class NumberUtil {
    public static String formatNumber(int number) {
        if (number < 10000) {
            return String.valueOf(number);
        }

        final boolean lessThanOneMillion = number < 1_000_000;
        return String.format(Locale.getDefault(), lessThanOneMillion ? "%.1fK" : "%.1fM", number / (lessThanOneMillion ? 1000.0 : 1_000_000.0));
    }
}
