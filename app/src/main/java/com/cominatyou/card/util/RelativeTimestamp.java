package com.cominatyou.card.util;

import java.time.Instant;

public class RelativeTimestamp {
    public static String get(Instant instant) {
        long seconds = Instant.now().getEpochSecond() - instant.getEpochSecond();
        if (seconds < 60) {
            return seconds + "s";
        }
        else if (seconds < 3600) {
            return seconds / 60 + "m";
        }
        else if (seconds < 86400) {
            return seconds / 3600 + "h";
        }
        else {
            return seconds / 86400 + "d";
        }
    }
}
