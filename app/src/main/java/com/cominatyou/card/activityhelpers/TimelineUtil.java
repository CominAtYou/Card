package com.cominatyou.card.activityhelpers;

import android.graphics.Typeface;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.cominatyou.card.JsonNetworkRequest;
import com.cominatyou.card.MainActivity;
import com.cominatyou.card.R;
import com.cominatyou.card.TimelineFragment;
import com.cominatyou.card.adapters.TimelineAdapter;
import com.cominatyou.card.data.Tweet;
import com.cominatyou.card.util.DataCache;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONArray;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

public class TimelineUtil {
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);

    static {
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static void getTimeline(TimelineFragment fragment, Runnable callback) {
        final String cachedTimeline = DataCache.get(fragment.requireContext(), "home_timeline.json");
        if (cachedTimeline != null) {
            fragment.binding.timelineRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));

            try {
                JSONArray cachedTimelineArray = new JSONArray(cachedTimeline);
                fragment.binding.timelineRecyclerView.setAdapter(new TimelineAdapter(cachedTimelineArray, fragment.getParentFragmentManager()));
                fragment.requireActivity().runOnUiThread(callback);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        JsonNetworkRequest.getArray(fragment.requireContext(), "https://api.twitter.com/1.1/statuses/home_timeline.json?count=200&include_entities=true&tweet_mode=extended", response -> {
            if (!response.isPresent()) {
                displayError(fragment, "Something happened while trying to get your timeline.");
                return;
            }

            DataCache.set(fragment.requireContext(), "home_timeline.json", response.get().toString());
            fragment.binding.timelineRecyclerView.setLayoutManager(new LinearLayoutManager(fragment.requireContext()));
            fragment.binding.timelineRecyclerView.setAdapter(new TimelineAdapter(response.get(), fragment.getParentFragmentManager()));
            callback.run();
        });
    }

    public static void refreshTimeline(TimelineFragment fragment) {
        final Tweet tweet = ((TimelineAdapter) fragment.binding.timelineRecyclerView.getAdapter()).getItemAtIndex(0);
        final String since_id = tweet.getId();
        final String url = "https://api.twitter.com/1.1/statuses/home_timeline.json?count=200&include_entities=true&tweet_mode=extended&since_id=" + since_id;

        JsonNetworkRequest.getArray(fragment.requireContext(), url, response -> {
            if (!response.isPresent()) {
                displayError(fragment, "Something happened while trying to get new Tweets.");
                fragment.binding.fragmentTimelineSwipeRefreshLayout.setRefreshing(false);
                return;
            }

            if (response.get().length() == 0) {
                fragment.binding.fragmentTimelineSwipeRefreshLayout.setRefreshing(false);
                return;
            }

            final String cachedTimelineString = DataCache.get(fragment.requireContext(), "home_timeline.json");
            if (cachedTimelineString != null) {
                // first, convert the cachedTimelineString to a JSONArray and then into an ArrayList
                // then, add every tweet in the JSONArray in response.get() to the beginning of the ArrayList, in reverse order
                // then, convert the ArrayList back into a JSONArray and then into a String
                // then, write the String to the cache

                try {
                    JSONArray cachedTimeline = new JSONArray(cachedTimelineString);
                    JSONArray newTimeline = new JSONArray();

                    for (int i = 0; i < cachedTimeline.length(); i++) {
                        newTimeline.put(cachedTimeline.get(i));
                    }

                    for (int i = response.get().length() - 1; i >= 0; i--) {
                        newTimeline.put(response.get().get(i));
                    }

                    DataCache.set(fragment.requireContext(), "home_timeline.json", newTimeline.toString());
                }

                catch (Exception e) {
                    e.printStackTrace();
                }
            }


            ((TimelineAdapter) fragment.binding.timelineRecyclerView.getAdapter()).addToBeginning(response.get());
            fragment.binding.fragmentTimelineSwipeRefreshLayout.setRefreshing(false);
            fragment.binding.timelineRecyclerView.scrollToPosition(0);
        });
    }

    private static void displayError(TimelineFragment fragment, String message) {
        Snackbar snackbar = Snackbar.make(((MainActivity) fragment.requireActivity()).binding.mainActivityCoordinatorLayout, message, Snackbar.LENGTH_LONG);
        TextView textView = snackbar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
        Typeface font = ResourcesCompat.getFont(fragment.requireContext(), R.font.gs_text_regular);
        textView.setTypeface(font);

        snackbar.show();
    }
}
