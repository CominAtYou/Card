package com.cominatyou.card;

import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.WindowCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cominatyou.card.adapters.TimelineAdapter;
import com.cominatyou.card.databinding.ActivityProfileBinding;
import com.cominatyou.card.util.LinkUtil;
import com.google.android.material.color.DynamicColors;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivityIfAvailable(this);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        final String userId = getIntent().getStringExtra("user_id");
        final boolean isId = getIntent().getBooleanExtra("is_id", false);

        final String params = "?user.fields=created_at,description,entities,id,location,name,profile_image_url,protected,public_metrics,username,pinned_tweet_id,url";
        final String requestUrl = (isId ? "https://api.twitter.com/2/users/" : "https://api.twitter.com/2/users/by/username/") + userId + params;

        JsonNetworkRequest.getObject(this, requestUrl, responseObject -> {
            if (!responseObject.isPresent() || responseObject.get().has("errors")) {
                final JSONObject error = responseObject.get().optJSONArray("errors").optJSONObject(0);
                Snackbar snackbar = Snackbar.make(binding.getRoot(), ProfileActivityUtil.getProfileActivityError(this, error.optString("detail"), userId), Snackbar.LENGTH_INDEFINITE);
                TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                Typeface font = ResourcesCompat.getFont(this, R.font.gs_text_regular);
                textView.setTypeface(font);

                snackbar.show();
                return;
            }

            final JSONObject user = responseObject.get().optJSONObject("data");
            final String savedUserId = getSharedPreferences("user_data", MODE_PRIVATE).getString("id", null);
            if (!user.has("location")) {
                binding.profileLocationContainer.setVisibility(View.GONE);
            }
            if (user.optString("url").equals("")) {
                binding.profileUrlContainer.setVisibility(View.GONE);
            }
            if (!user.has("location") && user.optString("url").equals("")) {
                binding.profileDetails.setVisibility(View.GONE);
            }
            if (user.optString("description").equals("")) {
                binding.bioText.setVisibility(View.GONE);
            }
            if (savedUserId.equals(user.optString("id"))) {
                binding.profileFollowButton.setVisibility(View.GONE);
            }


            Picasso.get().load(user.optString("profile_image_url").replace("normal", "400x400")).into(binding.profileAvatar);
            binding.activityProfileToolbar.setTitle(user.optString("name"));
            binding.profileUsername.setText("@" + user.optString("username"));

            final JSONObject entities = user.optJSONObject("entities");
            if (entities != null && entities.has("description") && entities.optJSONObject("description").has("urls")) {
                final JSONArray urls = entities.optJSONObject("description").optJSONArray("urls");
                binding.bioText.setText(LinkUtil.addHyperlinks(urls, user.optString("description")));
                binding.bioText.setMovementMethod(LinkMovementMethod.getInstance());
            } else {
                binding.bioText.setText(user.optString("description"));
            }

            binding.profileLocationText.setText(user.optString("location"));

            if (!user.optString("url").equals("")) {
                final JSONArray urls = user.optJSONObject("entities").optJSONObject("url").optJSONArray("urls");
                for (int i = 0; i < urls.length(); i++) {
                    final JSONObject url = urls.optJSONObject(i);
                    if (url.optString("url").equals(user.optString("url"))) {
                        binding.profileUrlText.setText(url.optString("display_url"));
                        break;
                    }
                }

                binding.profileUrlText.setOnClickListener(v -> {
                    CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                    intent.launchUrl(this, Uri.parse(user.optString("url")));
                });
            }
            binding.profileUsername.setText("@" + user.optString("username"));
            binding.followerCount.setText(String.format(Locale.getDefault(), "%,d", user.optJSONObject("public_metrics").optInt("followers_count")));
            binding.followingCount.setText(String.format(Locale.getDefault(), "%,d", user.optJSONObject("public_metrics").optInt("following_count")));
            binding.profileCard.setVisibility(View.VISIBLE);


            final String id = user.optString("id");
            JsonNetworkRequest.getArray(this, "https://api.twitter.com/1.1/friendships/lookup.json?user_id=" + id, response -> {
                if (!response.isPresent()) {
                    return;
                }

                final JSONArray connections = response.get().optJSONObject(0).optJSONArray("connections");
                for (int i = 0; i < connections.length(); i++) {
                    if (connections.optString(i).equals("following")) {
                        binding.profileFollowButton.setText(getString(R.string.activity_profile_following_label));
                        break;
                    }
                }

                JsonNetworkRequest.getArray(this, "https://api.twitter.com/1.1/statuses/user_timeline.json?tweet_mode=extended&count=100&include_rts=true&exclude_replies=true&user_id=" + id, timelineRes -> {
                    if (!timelineRes.isPresent()) {
                        return;
                    }

                    final JSONArray timeline = timelineRes.get();
                    binding.profileTimeline.setLayoutManager(new LinearLayoutManager(this));
                    binding.profileTimeline.setAdapter(new TimelineAdapter(timeline));
                    binding.activityProfileProgressbar.setVisibility(View.GONE);
                });
            });

            binding.activityProfileToolbar.setNavigationOnClickListener(v -> finish());
        });


        binding.appBarLayout.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            float range = (float) -appBarLayout.getTotalScrollRange();
            binding.profileAvatar.setImageAlpha((int) (255 * (1f - (float) verticalOffset / range)));
            binding.profileUsername.setAlpha(1f - (float) verticalOffset / range);
        });
    }
}
