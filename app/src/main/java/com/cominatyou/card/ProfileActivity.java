package com.cominatyou.card;

import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.view.WindowCompat;

import com.cominatyou.card.auth.TokenManager;
import com.cominatyou.card.databinding.ActivityProfileBinding;
import com.cominatyou.card.util.GlobalHttpClient;
import com.cominatyou.card.util.LinkUtil;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Locale;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ProfileActivity extends AppCompatActivity {
    private ActivityProfileBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        final String userId = getIntent().getStringExtra("user_id");
        final boolean isId = getIntent().getBooleanExtra("is_id", false);

        final OkHttpClient client = GlobalHttpClient.getInstance();
        final String path = "/?user.fields=created_at,description,entities,id,location,name,profile_image_url,protected,public_metrics,username,pinned_tweet_id,url";
        final Request request = new Request.Builder()
                .url((isId ? "https://api.twitter.com/2/users/" : "https://api.twitter.com/2/users/by/username/") + userId + path)
                .addHeader("Authorization", "Bearer " + TokenManager.get(this))
                .build();

        new Thread(() -> {
            if (TokenManager.isExpired(this)) {
                try {
                    TokenManager.refresh(this);
                    runOnUiThread(() -> Toast.makeText(this, "Successfully refreshed access token", Toast.LENGTH_SHORT).show());
                }
                catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(this, "Failed to refresh token", Toast.LENGTH_SHORT).show());
                    return;
                }
            }

            try (Response response = client.newCall(request).execute()) {
                final String data = response.body().string();
                final JSONObject user = new JSONObject(data).getJSONObject("data");
                runOnUiThread(() -> {
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

                    Picasso.get().load(user.optString("profile_image_url").replace("normal", "400x400")).into(binding.profileAvatar);
                    binding.activityProfileToolbar.setTitle(user.optString("name"));
                    binding.profileUsername.setText("@" + user.optString("username"));

                    final JSONObject entities = user.optJSONObject("entities");
                    if (entities != null && entities.has("description") && entities.optJSONObject("description").has("urls")) {
                        final JSONArray urls = entities.optJSONObject("description").optJSONArray("urls");
                        binding.bioText.setText(LinkUtil.addHyperlinks(urls, user.optString("description")));
                        binding.bioText.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    else {
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
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(this, "Unable to get profile details", Toast.LENGTH_SHORT).show());
            }
        }).start();

        binding.activityProfileToolbar.setNavigationOnClickListener(v -> finish());
    }
}
