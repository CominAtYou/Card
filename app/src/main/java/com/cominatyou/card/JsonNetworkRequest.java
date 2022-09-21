package com.cominatyou.card;

import android.content.Context;

import com.cominatyou.card.auth.TokenManager;
import com.cominatyou.card.util.GlobalHttpClient;

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
        final OkHttpClient client = GlobalHttpClient.getInstance();

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + TokenManager.get(context))
                .build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONObject(response.body().string());
        }
    }

    public static JSONArray getArray(Context context, String url) throws IOException, JSONException {
        final OkHttpClient client = GlobalHttpClient.getInstance();
        final String token = context.getSharedPreferences("auth", Context.MODE_PRIVATE).getString("access_token", null);

        final Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + token)
                .build();

        try (Response response = client.newCall(request).execute()) {
            return new JSONArray(response.body().string());
        }
    }
}
