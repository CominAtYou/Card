package com.cominatyou.card;

import android.content.Context;

import java.util.Map;

public class ProfileActivityUtil {
    private static final Map<String, Integer> errorMessages = Map.of(
            "User has been suspended", R.string.activity_profile_user_suspended_message,
            "Could not find user with username", R.string.activity_profile_cannot_find_user_message
    );

    public static String getProfileActivityError(Context context, String error, String username) {
        final String key = error.contains(": ") ? error.substring(0, error.indexOf(": ")) : error;
        if (errorMessages.containsKey(key)) {
            return context.getString(errorMessages.get(key), username);
        }
        else {
            return error;
        }
    }
}
