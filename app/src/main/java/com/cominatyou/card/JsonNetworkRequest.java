package com.cominatyou.card;

import android.content.Context;

import com.cominatyou.card.auth.TokenManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class JsonNetworkRequest {
    public static JSONObject getObject(Context context, String url) throws IOException, JSONException {
        // send a GET request using the url, and return a json object
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + TokenManager.get(context))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONObject(response.body().string());
        }
    }

    public static JSONArray getArray(Context context, String url) throws IOException, JSONException {
        OkHttpClient client = new OkHttpClient();
        final String token = context.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("access_token", null);

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONArray(response.body().string());
        }
    }
}
