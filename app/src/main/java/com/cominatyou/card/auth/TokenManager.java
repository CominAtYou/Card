package com.cominatyou.card.auth;

import android.content.Context;

import com.cominatyou.card.util.Config;
import com.cominatyou.card.util.GlobalHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.Instant;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class TokenManager {
    public static String get(Context context) {
        return context.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("access_token", null);
    }

    public static void refresh(Context context) throws Exception {
        final String refreshToken = context.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("refresh_token", null);

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

        context.getSharedPreferences("auth", Context.MODE_PRIVATE).edit()
                .putString("access_token", respJson.getString("access_token"))
                .putString("refresh_token", respJson.getString("refresh_token"))
                .putLong("expires_at", Instant.now().getEpochSecond() + respJson.getInt("expires_in"))
                .apply();
    }

    public static boolean isExpired(Context context) {
        return Instant.now().getEpochSecond() > context.getSharedPreferences("auth", Context.MODE_PRIVATE).getLong("expires_at", 0);
    }
}
