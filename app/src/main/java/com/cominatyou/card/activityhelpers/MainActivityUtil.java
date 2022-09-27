package com.cominatyou.card.activityhelpers;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.cominatyou.card.JsonNetworkRequest;
import com.cominatyou.card.MainActivity;
import com.cominatyou.card.R;
import com.cominatyou.card.TimelineFragment;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

public class MainActivityUtil {
    public static void getUserData(MainActivity activity) {

        JsonNetworkRequest.getObject(activity, "https://api.twitter.com/2/users/me?user.fields=profile_image_url,name,username,id", response -> {
            SharedPreferences userData = activity.getSharedPreferences("user_data", Context.MODE_PRIVATE);
            if (!response.isPresent()) {
                Snackbar snackbar = Snackbar.make(activity.binding.mainActivityCoordinatorLayout, "Something happened while trying to get your account info.", Snackbar.LENGTH_INDEFINITE);
                TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                Typeface font = ResourcesCompat.getFont(activity, R.font.gs_text_regular);
                textView.setTypeface(font);

                snackbar.show();
                return;
            }


            JSONObject me = response.get().optJSONObject("data");
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
