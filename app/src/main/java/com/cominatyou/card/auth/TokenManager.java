package com.cominatyou.card.auth;

import android.content.Context;

import com.cominatyou.card.util.Config;
import com.cominatyou.card.util.GlobalHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TokenManager {
    public static String getToken(Context context) {
        try {
            final byte[] jsonBytes = Files.readAllBytes(new File(context.getFilesDir(), "auth.json").toPath());
            final JSONObject json = new JSONObject(new String(jsonBytes));
            return json.getString("token");
        }
        catch (Exception ignored) {
            return null;
        }
    }

    public static String getTokenSecret(Context context) {
        try {
            final byte[] jsonBytes = Files.readAllBytes(new File(context.getFilesDir(), "auth.json").toPath());
            final JSONObject json = new JSONObject(new String(jsonBytes));
            return json.getString("token_secret");
        }
        catch (Exception ignored) {
            return null;
        }
    }
}
