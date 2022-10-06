package com.cominatyou.card;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cominatyou.card.adapters.ProfileAdapter;
import com.cominatyou.card.data.Profile;
import com.cominatyou.card.databinding.ActivityProfileBinding;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        binding.activityProfileToolbar.setNavigationOnClickListener(v -> finish());

        final String userId = getIntent().getStringExtra("user_id");
        final boolean isId = getIntent().getBooleanExtra("is_id", false);

        final String params = "?user.fields=created_at,description,entities,id,location,name,profile_image_url,protected,public_metrics,username,pinned_tweet_id,url";
        final String requestUrl = (isId ? "https://api.twitter.com/2/users/" : "https://api.twitter.com/2/users/by/username/") + userId + params;

        ProfileActivityUtil.getProfile(this, userId, isId, response -> {
            if (!response.isPresent()) {
                Snackbar snackbar = Snackbar.make(binding.getRoot(), "There was an error trying to get the profile.", Snackbar.LENGTH_INDEFINITE);
                TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                Typeface font = ResourcesCompat.getFont(this, R.font.gs_text_regular);
                textView.setTypeface(font);

                snackbar.show();
                return;
            }

            final Profile profile = response.get();
            Picasso.get().load(profile.getProfileImageUrl().replace("normal", "400x400")).into(binding.profileAvatar);
            binding.activityProfileToolbar.setTitle(profile.getName());
            binding.profileUsername.setText("@" + profile.getHandle());
            JsonNetworkRequest.getArray(ProfileActivity.this, "https://api.twitter.com/1.1/statuses/user_timeline.json?tweet_mode=extended&count=100&include_rts=true&exclude_replies=true&user_id=" + profile.getId(), timelineRes -> {
                if (!timelineRes.isPresent()) {
                    return;
                }

                final JSONArray timeline = timelineRes.get();
                binding.profileTimeline.setLayoutManager(new LinearLayoutManager(this));
                binding.profileTimeline.setAdapter(new ProfileAdapter(this, profile, timeline));
                binding.activityProfileProgressbar.setVisibility(View.GONE);
            });
        });

        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float range = (float) -appBarLayout.getTotalScrollRange();
            binding.profileAvatar.setImageAlpha((int) (255 * (1f - (float) verticalOffset / range)));
            binding.profileUsername.setAlpha(1f - (float) verticalOffset / range);
        });
    }
}
