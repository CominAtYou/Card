package com.cominatyou.card.activityhelpers;

import android.content.Context;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.cominatyou.card.JsonNetworkRequest;
import com.cominatyou.card.TimelineFragment;
import com.cominatyou.card.adapters.TimelineAdapter;
import com.cominatyou.card.auth.TokenManager;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimelineUtil {
    private static DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    static {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static void getTimeline(TimelineFragment fragment, Runnable callback) {
        final String user_id = fragment.requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("id", null);
        final String url =  "https://api.twitter.com/2/users/" + user_id + "/timelines/reverse_chronological?tweet.fields=created_at,text,referenced_tweets,entities,public_metrics,id&expansions=entities.mentions.username,author_id,referenced_tweets.id,referenced_tweets.id.author_id,in_reply_to_user_id&user.fields=profile_image_url,name,username,id&media.fields=duration_ms,preview_image_url,type,url";

        new Thread(() -> {
            try {
                final JSONObject response = JsonNetworkRequest.getObject(fragment.requireContext(), url);

                fragment.requireActivity().runOnUiThread(() -> {
                    fragment.binding.timelineRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
                    fragment.binding.timelineRecyclerView.setAdapter(new TimelineAdapter(response));
                    fragment.requireContext().getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("last_timeline_update", df.format(new Date())).apply();
                    callback.run();
                });
            }
            catch (Exception e) {
                fragment.requireActivity().runOnUiThread(() -> Toast.makeText(fragment.requireContext(), "Failed to get timeline", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    public static void refreshTimeline(TimelineFragment fragment) {
        final String lastTimelineUpdate = fragment.requireContext().getSharedPreferences("config", Context.MODE_PRIVATE).getString("last_timeline_update", null);
        long expiresAt = fragment.requireContext().getSharedPreferences("auth", Context.MODE_PRIVATE).getLong("expires_at", 0);

        final String user_id = fragment.requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("id", null);
        final String url =  "https://api.twitter.com/2/users/" + user_id + "/timelines/reverse_chronological?start_time=" + lastTimelineUpdate + "&tweet.fields=created_at,text,referenced_tweets,entities,public_metrics,id&expansions=entities.mentions.username,author_id,referenced_tweets.id,referenced_tweets.id.author_id,in_reply_to_user_id&user.fields=profile_image_url,name,username,id&media.fields=duration_ms,preview_image_url,type,url";

        new Thread(() -> {
            try {
                if (Instant.now().getEpochSecond() > expiresAt) {
                    TokenManager.refresh(fragment.requireContext());
                }

                final JSONObject response = JsonNetworkRequest.getObject(fragment.requireContext(), url);
                if (response.optJSONObject("meta").getInt("result_count") == 0) {
                    fragment.binding.fragmentTimelineSwipeRefreshLayout.setRefreshing(false);
                    return;
                }

                fragment.requireActivity().runOnUiThread(() -> {
                    ((TimelineAdapter) fragment.binding.timelineRecyclerView.getAdapter()).addToBeginning(response);
                    fragment.requireContext().getSharedPreferences("config", Context.MODE_PRIVATE).edit().putString("last_timeline_update", df.format(new Date())).apply();
                    fragment.binding.fragmentTimelineSwipeRefreshLayout.setRefreshing(false);
                    fragment.binding.timelineRecyclerView.scrollToPosition(0);
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                fragment.requireActivity().runOnUiThread(() -> Toast.makeText(fragment.requireContext(), "Something happened while trying to get new Tweets.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
}
