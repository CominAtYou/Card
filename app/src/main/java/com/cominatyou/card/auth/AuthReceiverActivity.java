package com.cominatyou.card.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cominatyou.card.MainActivity;
import com.cominatyou.card.util.Config;
import com.cominatyou.card.R;
import com.cominatyou.card.databinding.ActivityAuthReceiverBinding;
import com.google.android.material.color.DynamicColors;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.Response;

public class AuthReceiverActivity extends AppCompatActivity {
    private ActivityAuthReceiverBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityAuthReceiverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String codeVerifier = getSharedPreferences("auth", MODE_PRIVATE).getString("code_verifier", null);
        Uri data = getIntent().getData();
        String code = data.getQueryParameter("code");

        String urlParameters = "grant_type=authorization_code&code=" + code + "&redirect_uri=com.cominatyou.card-auth://callback&client_id=" + Config.CLIENT_ID + "&code_verifier=" + codeVerifier;
        final String url = "https://api.twitter.com/2/oauth2/token";
        final MediaType formUrlEnconded = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

        OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .post(RequestBody.create(urlParameters, formUrlEnconded))
                .build();

        new Thread(() -> {
            try {
                Response response = client.newCall(request).execute();
                saveCredentials(response.body().string());
            }
            catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> getSupportFragmentManager().beginTransaction().replace(R.id.auth_receiver_root_fragment, new AuthFailedFragment()).commit());
            }
        }).start();
    }

    private void saveCredentials(String json) throws JSONException {
        JSONObject result = new JSONObject(json);

        getSharedPreferences("auth", MODE_PRIVATE).edit()
                .putString("access_token", result.getString("access_token"))
                .putString("refresh_token", result.getString("refresh_token"))
                .putLong("expires_at", Instant.now().getEpochSecond() + result.getInt("expires_in"))
                .remove("code_verifier")
                .apply();

        getSharedPreferences("config", MODE_PRIVATE).edit().putBoolean("authenticated", true).apply();

        runOnUiThread(() -> Toast.makeText(this, "Successfully logged in to Twitter", Toast.LENGTH_SHORT).show());
        startActivity(new Intent(this, MainActivity.class));
    }
}
