package com.cominatyou.card.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.cominatyou.card.MainActivity;
import com.cominatyou.card.R;
import com.cominatyou.card.databinding.ActivityAuthReceiverBinding;
import com.google.android.material.color.DynamicColors;

import org.json.JSONObject;

import java.nio.file.Files;

public class AuthReceiverActivity extends AppCompatActivity {
    private ActivityAuthReceiverBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityAuthReceiverBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Uri data = getIntent().getData();
        String code = data.getQueryParameter("oauth_verifier");

        new Thread(() -> {
            try {
                Auth.getProvider().retrieveAccessToken(Auth.getConsumer(), code);
            }
            catch (Exception e) {
                runOnUiThread(() -> getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, AuthFailedFragment.class, null).commit());
            }

            final String token = Auth.getConsumer().getToken();
            final String tokenSecret = Auth.getConsumer().getTokenSecret();

            final JSONObject authData = new JSONObject();
            try {
                authData.put("token", token);
                authData.put("token_secret", tokenSecret);

                Files.write(getFilesDir().toPath().resolve("auth.json"), authData.toString().getBytes());

                getSharedPreferences("config", MODE_PRIVATE).edit().putBoolean("authenticated", true).apply();
                runOnUiThread(() -> startActivity(new Intent(this, MainActivity.class)));
                finish();
            }
            catch (Exception e) {
                runOnUiThread(() -> getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, AuthFailedFragment.class, null).commit());
            }
        }).start();
    }
}
