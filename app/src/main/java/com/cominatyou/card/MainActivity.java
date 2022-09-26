package com.cominatyou.card;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.cominatyou.card.activityhelpers.BottomNavigationFragmentManager;
import com.cominatyou.card.activityhelpers.MainActivityUtil;
import com.cominatyou.card.auth.Auth;
import com.cominatyou.card.auth.TokenManager;
import com.cominatyou.card.databinding.ActivityMainBinding;
import com.google.android.material.color.DynamicColors;

public class MainActivity extends AppCompatActivity {
    public ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        binding.bottomNavigation.setOnItemSelectedListener(item -> BottomNavigationFragmentManager.switchFragment(this, item));

        SharedPreferences config = getSharedPreferences("config", MODE_PRIVATE);
        SharedPreferences userData = getSharedPreferences("user_data", MODE_PRIVATE);

        if (!config.getBoolean("authenticated", false)) {
            try {
                Auth.authenticate(this);
            }
            catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to authenticate", Toast.LENGTH_SHORT).show();
            }
        }
        else if (userData.getString("id", null)  == null) {
            MainActivityUtil.getUserData(this);
        }
        else if (getSupportFragmentManager().getFragments().size() == 0) {
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, TimelineFragment.class, null, "timeline").commit();
        }
    }
}