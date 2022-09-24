package com.cominatyou.card.activityhelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.cominatyou.card.JsonNetworkRequest;
import com.cominatyou.card.MainActivity;
import com.cominatyou.card.R;
import com.cominatyou.card.TimelineFragment;
import com.cominatyou.card.auth.TokenManager;

import org.json.JSONObject;

public class MainActivityUtil {
    public static void getUserData(MainActivity activity) {
        SharedPreferences userData = activity.getSharedPreferences("user_data", Context.MODE_PRIVATE);
        new Thread(() -> {
            try {
                JSONObject response = JsonNetworkRequest.getObject(activity, "https://api.twitter.com/2/users/me?user.fields=profile_image_url,name,username,id");
                JSONObject me = response.getJSONObject("data");

                userData.edit()
                        .putString("id", me.getString("id"))
                        .putString("name", me.getString("name"))
                        .putString("handle", me.getString("username"))
                        .putString("profile_image_url", me.getString("profile_image_url"))
                        .apply();

                activity.runOnUiThread(() -> activity.getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, TimelineFragment.class, null, "timeline").commit());
            }
            catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> Toast.makeText(activity, "Failed to get user data", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public static void refreshToken(MainActivity activity) {
        new Thread(() -> {
            try {
                TokenManager.refresh(activity);
                activity.runOnUiThread(() -> activity.getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, TimelineFragment.class, null, "timeline").commit());
                activity.runOnUiThread(() -> Toast.makeText(activity, "Successfully refreshed access token", Toast.LENGTH_SHORT).show());
            }
            catch (Exception e) {
                e.printStackTrace();
                activity.runOnUiThread(() -> Toast.makeText(activity, "Failed to refresh token", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
