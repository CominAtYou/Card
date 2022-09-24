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
    public static String get(Context context) {
        try {
            final byte[] jsonBytes = Files.readAllBytes(new File(context.getFilesDir(), "auth.json").toPath());
            final JSONObject json = new JSONObject(new String(jsonBytes));
            return json.getString("access_token");
        }
        catch (Exception ignored) {
            return null;
        }
    }

    public static void refresh(Context context) throws Exception {
        final String refreshToken = getRefreshToken(context);

        final OkHttpClient client = GlobalHttpClient.getInstance();
        final String url = "https://api.twitter.com/2/oauth2/token";
        final String urlParameters = "grant_type=refresh_token&refresh_token=" + refreshToken + "&client_id=" + Config.CLIENT_ID;

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(urlParameters, MediaType.parse("application/x-www-form-urlencoded")))
                .build();

        okhttp3.Response response = client.newCall(request).execute();
        final JSONObject respJson = new JSONObject(response.body().string());

        if (!response.isSuccessful()) {
            throw new Exception("Failed to refresh token: " + respJson);
        }

        final long expiresAt = Instant.now().getEpochSecond() + respJson.getInt("expires_in");
        respJson.put("expires_at", expiresAt);
        respJson.remove("expires_in");
        respJson.remove("scope");
        respJson.remove("token_type");

        final File authData = new File(context.getFilesDir(), "auth.json");
        Files.write(authData.toPath(), respJson.toString().getBytes());
    }

    public static boolean isExpired(Context context) {
        try {
            final byte[] jsonBytes = Files.readAllBytes(new File(context.getFilesDir(), "auth.json").toPath());
            final JSONObject json = new JSONObject(new String(jsonBytes));
            return Instant.now().getEpochSecond() > json.getLong("expires_at");
        }
        catch (Exception ignored) {
            return true;
        }
    }

    private static String getRefreshToken(Context context) throws IOException, JSONException {
        final byte[] jsonBytes = Files.readAllBytes(new File(context.getFilesDir(), "auth.json").toPath());
        final JSONObject json = new JSONObject(new String(jsonBytes));
        return json.getString("refresh_token");
    }
}
