package com.cominatyou.card;

import android.content.Context;
import android.os.Handler;

import com.cominatyou.card.util.GlobalHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.ByteString;

public class JsonNetworkRequest {
    public static void getObject(Context context, String url, Consumer<JSONObject> callback) {
        new Thread(() -> {
            final OkHttpClient client = GlobalHttpClient.getInstance(context).getClient();

            final Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();
                JSONObject object = new JSONObject(response.body().string());
                new Handler(context.getMainLooper()).post(() -> callback.accept(object));
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
                callback.accept(null);
            }
        }).start();
    }

    public static void getArray(Context context, String url, Consumer<JSONArray> callback) {
        new Thread(() -> {
            final OkHttpClient client = GlobalHttpClient.getInstance(context).getClient();
            final Request request = new Request.Builder().url(url).build();

            try {
                Response response = client.newCall(request).execute();
                JSONArray array = new JSONArray(response.body().string());
                new Handler(context.getMainLooper()).post(() -> callback.accept(array));
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
                callback.accept(null);
            }
        }).start();
    }

    public static void postObject(Context context, String url, JSONObject object, Consumer<JSONObject> callback) {
        new Thread(() -> {
            final OkHttpClient client = GlobalHttpClient.getInstance(context).getClient();
            final MediaType type = MediaType.parse("application/json; charset=utf-8");
            final Request request = new Request.Builder().url(url).post(RequestBody.create(object.toString().getBytes(), type)).build();

            try {
                final Response response = client.newCall(request).execute();
                final JSONObject responseObject = new JSONObject(response.body().string());
                new Handler(context.getMainLooper()).post(() -> callback.accept(responseObject));
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
                new Handler(context.getMainLooper()).post(() -> callback.accept(null));
            }
        }).start();
    }

    public static void sendDelete(Context context, String url, Consumer<JSONObject> callback) {
        new Thread(() -> {
            final OkHttpClient client = GlobalHttpClient.getInstance(context).getClient();
            final Request request = new Request.Builder().url(url).delete().build();

            try {
                final Response response = client.newCall(request).execute();
                final JSONObject responseObject = new JSONObject(response.body().string());
                new Handler(context.getMainLooper()).post(() -> callback.accept(responseObject));
            }
            catch (IOException | JSONException e) {
                e.printStackTrace();
                new Handler(context.getMainLooper()).post(() -> callback.accept(null));
            }
        }).start();
    }
}
