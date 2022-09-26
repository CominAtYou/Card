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

        JsonNetworkRequest.getObject(activity, "https://api.twitter.com/2/users/me?user.fields=profile_image_url,name,username,id", response -> {
            SharedPreferences userData = activity.getSharedPreferences("user_data", Context.MODE_PRIVATE);
            JSONObject me = response.optJSONObject("data");
            assert me != null;
            userData.edit()
                    .putString("id", me.optString("id"))
                    .putString("name", me.optString("name"))
                    .putString("handle", me.optString("username"))
                    .putString("profile_image_url", me.optString("profile_image_url"))
                    .apply();

            activity.getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, TimelineFragment.class, null, "timeline").commit();
        });
    }
}
