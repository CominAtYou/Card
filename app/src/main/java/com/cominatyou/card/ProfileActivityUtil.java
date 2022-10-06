package com.cominatyou.card;

import android.content.Context;

import com.cominatyou.card.data.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class ProfileActivityUtil {
    private static final Map<String, Integer> errorMessages = Map.of(
            "User has been suspended", R.string.activity_profile_user_suspended_message,
            "Could not find user with username", R.string.activity_profile_cannot_find_user_message
    );

    public static String getProfileActivityError(Context context, String error, String username) {
        final String key = error.contains(": ") ? error.substring(0, error.indexOf(": ")) : error;
        if (errorMessages.containsKey(key)) {
            return context.getString(errorMessages.get(key), username);
        }
        else {
            return error;
        }
    }

    public static void getProfile(Context context, String identifier, boolean isId, Consumer<Optional<Profile>> callback) {
        JsonNetworkRequest.getObject(context, (isId ? "https://api.twitter.com/2/users/" : "https://api.twitter.com/2/users/by/username/") + identifier + "?user.fields=created_at,description,entities,id,location,name,profile_image_url,protected,public_metrics,username,pinned_tweet_id,url", response -> {
            if (!response.isPresent()) {
                callback.accept(Optional.empty());
                return;
            }

            final JSONObject user = response.get().optJSONObject("data");
            final String name = user.optString("name");
            final String username = user.optString("username");
            final String id = user.optString("id");
            final String description = user.optString("description");
            final String profileImageUrl = user.optString("profile_image_url");
            final String location = user.optString("location");
            final String url = user.optString("url");
            final int followersCount = user.optJSONObject("public_metrics").optInt("followers_count");
            final int followingCount = user.optJSONObject("public_metrics").optInt("following_count");

            final JSONObject entities = user.optJSONObject("entities");
            JSONArray descriptionUrls = new JSONArray();
            if (entities != null && entities.has("description") && entities.optJSONObject("description").has("urls")) {
                descriptionUrls = entities.optJSONObject("description").optJSONArray("urls");
            }

            String displayUrl = "";
            if (!url.isEmpty()) {
                final JSONArray urls = entities.optJSONObject("url").optJSONArray("urls");
                for (int i = 0; i < urls.length(); i++) {
                    final JSONObject currentUrl = urls.optJSONObject(i);
                    if (currentUrl.optString("url").equals(url)) {
                        displayUrl = currentUrl.optString("display_url");
                        break;
                    }
                }
            }

            final Profile profile = new Profile(name, username, id, description, url, displayUrl, descriptionUrls, location, profileImageUrl, followersCount, followingCount);

            callback.accept(Optional.of(profile));
        });
    }
}
