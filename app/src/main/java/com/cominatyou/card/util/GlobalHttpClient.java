package com.cominatyou.card.util;

import okhttp3.OkHttpClient;

public class GlobalHttpClient {
    private static OkHttpClient client = new OkHttpClient();

    public static OkHttpClient getInstance() {
        return client;
    }
}
