package com.cominatyou.card.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

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
        else if (seconds < 259200) {
            return seconds / 86400 + "d";
        }
        else {
            final ZonedDateTime tweetTime = instant.atZone(ZoneId.systemDefault());
            final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
            final String month = DateTimeFormatter.ofPattern("MMM").format(tweetTime);

            if (tweetTime.getYear() == now.getYear()) {
                if (Locale.getDefault().equals(Locale.US)) {
                    return month + " " + tweetTime.getDayOfMonth();
                }
                else {
                    return tweetTime.getDayOfMonth() + " " + month;
                }
            }
            else {
                if (Locale.getDefault().equals(Locale.US)) {
                    return month + " " + tweetTime.getDayOfMonth() + ", " + tweetTime.getYear();
                }
                else {
                    return tweetTime.getDayOfMonth() + " " + month + " " + tweetTime.getYear();
                }
            }
        }
    }
}
