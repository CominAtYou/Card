package com.cominatyou.card.util;

import android.content.Context;

import com.cominatyou.card.auth.TokenManager;

import okhttp3.OkHttpClient;
import se.akerfeldt.okhttp.signpost.OkHttpOAuthConsumer;
import se.akerfeldt.okhttp.signpost.SigningInterceptor;

public class GlobalHttpClient {
    private static GlobalHttpClient instance = null;
    private final OkHttpClient client;

    private GlobalHttpClient(Context context) {
        OkHttpOAuthConsumer consumer = new OkHttpOAuthConsumer(Config.CONSUMER_KEY, Config.CONSUMER_SECRET);
        consumer.setTokenWithSecret(TokenManager.getToken(context), TokenManager.getTokenSecret(context));
        client = new OkHttpClient.Builder().addInterceptor(new SigningInterceptor(consumer)).build();
    }

    public OkHttpClient getClient() {
        return client;
    }

    public static GlobalHttpClient getInstance(Context context) {
        if (instance == null) {
            instance = new GlobalHttpClient(context);
            return instance;
        }
        return instance;
    }

}
