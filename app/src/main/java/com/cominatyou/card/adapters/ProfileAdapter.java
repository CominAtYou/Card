package com.cominatyou.card.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.RecyclerView;

import com.cominatyou.card.JsonNetworkRequest;
import com.cominatyou.card.ProfileActivity;
import com.cominatyou.card.R;
import com.cominatyou.card.activityhelpers.ReplyBottomSheet;
import com.cominatyou.card.data.Profile;
import com.cominatyou.card.data.Tweet;
import com.cominatyou.card.util.DataCache;
import com.cominatyou.card.util.LinkOnTouchListener;
import com.cominatyou.card.util.LinkUtil;
import com.cominatyou.card.util.NumberUtil;
import com.cominatyou.card.util.RelativeTimestamp;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Object> tweets = new ArrayList<>();
    private final Context context;

    public ProfileAdapter(Context context, Profile profile, JSONArray timeline) {
        this.context = context;
        tweets.add(profile);

        for (int i = 0; i < timeline.length(); i++) {
            tweets.add(new Tweet(timeline.optJSONObject(i)));
        }
    }

    public static class TimelineTweetHolder extends RecyclerView.ViewHolder {
        protected final TextView tweetText;
        protected final ShapeableImageView profileImage;
        protected final TextView authorName;
        protected final TextView authorUsername;
        protected final TextView timestamp;
        protected final LinearLayout tweetAction;
        protected final TextView actionLabel;
        protected final Chip retweetButton;
        protected final Chip replyButton;
        protected final Chip likeButton;

        public TimelineTweetHolder(View itemView) {
            super(itemView);

            tweetText = itemView.findViewById(R.id.tweet_text_content);
            profileImage = itemView.findViewById(R.id.tweet_author_avatar);
            authorName = itemView.findViewById(R.id.tweet_author_name);
            authorUsername = itemView.findViewById(R.id.tweet_author_handle);
            timestamp = itemView.findViewById(R.id.tweet_timestamp);
            tweetAction = itemView.findViewById(R.id.tweet_action);
            actionLabel = itemView.findViewById(R.id.tweet_action_label);
            retweetButton = itemView.findViewById(R.id.tweet_retweet_chip);
            replyButton = itemView.findViewById(R.id.tweet_reply_chip);
            likeButton = itemView.findViewById(R.id.tweet_like_chip);
        }
    }

    public static class SingleMediaItemTimelineTweetHolder extends TimelineTweetHolder {
        ShapeableImageView imageView;

        public SingleMediaItemTimelineTweetHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.tweet_media);
        }
    }

    public static class ProfileDetailsView extends RecyclerView.ViewHolder {
        protected final TextView description;
        protected final LinearLayout details;
        protected final LinearLayout locationContainer;
        protected final TextView locationText;
        protected final LinearLayout urlContainer;
        protected final TextView urlText;
        protected final TextView followers;
        protected final TextView following;
        protected final ChipGroup buttonsContainer;
        protected final Chip followButton;

        public ProfileDetailsView(View itemView) {
            super(itemView);

            this.description = itemView.findViewById(R.id.profile_description);
            this.details = itemView.findViewById(R.id.profile_details);
            this.locationContainer = itemView.findViewById(R.id.profile_location_container);
            this.locationText = itemView.findViewById(R.id.profile_location_text);
            this.urlContainer = itemView.findViewById(R.id.profile_url_container);
            this.urlText = itemView.findViewById(R.id.profile_url_text);
            this.followers = itemView.findViewById(R.id.follower_count);
            this.following = itemView.findViewById(R.id.following_count);
            this.buttonsContainer = itemView.findViewById(R.id.profile_buttons_container);
            this.followButton = itemView.findViewById(R.id.profile_follow_button);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        switch (viewType) {
            case -1: {
                return new ProfileDetailsView(inflater.inflate(R.layout.profile_detail_card, parent,false));
            }
            case 1: {
                return new SingleMediaItemTimelineTweetHolder(inflater.inflate(R.layout.tweet_single_image, parent, false));
            }
            default: {
                return new TimelineTweetHolder(inflater.inflate(R.layout.tweet_text_only, parent, false));
            }
        }
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        final String currentUserId = context.getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("id", null);
        if (holder instanceof ProfileDetailsView) {
            final ProfileDetailsView vh = (ProfileDetailsView) holder;
            final Profile profile = (Profile) tweets.get(position);
            if (profile.getLocation().isEmpty()) {
                vh.locationContainer.setVisibility(View.GONE);
            }
            if (profile.getUrl().isEmpty()) {
                vh.urlContainer.setVisibility(View.GONE);
            }
            if (profile.getUrl().isEmpty() && profile.getLocation().isEmpty()) {
                vh.details.setVisibility(View.GONE);
            }
            if (profile.getId().equals(currentUserId)) {
                vh.buttonsContainer.setVisibility(View.GONE);
            }

            if (profile.getDescriptionUrls().length() > 0) {
                vh.description.setText(LinkUtil.addHyperlinks(profile.getDescriptionUrls(), profile.getDescription()));
                vh.description.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else {
                vh.description.setText(profile.getDescription());
            }

            vh.locationText.setText(profile.getLocation());
            vh.followers.setText(String.format(Locale.getDefault(), "%,d", profile.getFollowerCount()));
            vh.following.setText(String.format(Locale.getDefault(), "%,d", profile.getFollowingCount()));
            vh.urlText.setText(profile.getDisplayUrl());
            vh.urlText.setOnClickListener(e -> {
                final CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
                intent.launchUrl(context, Uri.parse(profile.getUrl()));
            });

            JsonNetworkRequest.getArray(context, "https://api.twitter.com/1.1/friendships/lookup.json?user_id=" + profile.getId(), response -> {
                if (!response.isPresent()) {
                    return;
                }

                final JSONArray connections = response.get().optJSONObject(0).optJSONArray("connections");
                for (int i = 0; i < connections.length(); i++) {
                    if (connections.optString(i).equals("following")) {
                        vh.followButton.setText(context.getString(R.string.activity_profile_following_label));
                        break;
                    }
                }
            });

            return;
        }


        final Tweet tweet = (Tweet) tweets.get(position);

        // Tweet content is HTML encoded. I don't know why.
        final String unescapedContent = StringEscapeUtils.unescapeHtml4(tweet.getText());

        if (holder instanceof SingleMediaItemTimelineTweetHolder) {
            final SingleMediaItemTimelineTweetHolder singleMediaItemTimelineTweetHolder = (SingleMediaItemTimelineTweetHolder) holder;
            final Tweet.Media media = tweet.getMedia().get(0);
            final String url = media.getPreviewImageUrl() == null ? media.getUrl() : media.getPreviewImageUrl();
            Picasso.get().load(url).into(singleMediaItemTimelineTweetHolder.imageView);

            if (tweet.getText().isEmpty()) singleMediaItemTimelineTweetHolder.tweetText.setVisibility(View.GONE);
            else singleMediaItemTimelineTweetHolder.tweetText.setVisibility(View.VISIBLE); // if this is a recycled view that had a previously hidden TextView, un-hide it
        }

        final TimelineTweetHolder vh = (TimelineTweetHolder) holder;
        if (tweet.getUrls().size() > 0) {
            final Spannable content = LinkUtil.addHyperlinks(tweet.getUrls(), unescapedContent);
            vh.tweetText.setText(content);
            vh.tweetText.setOnTouchListener(LinkOnTouchListener.getListener());
        }
        else {
            vh.tweetText.setText(unescapedContent);
        }

        Picasso.get().load(tweet.getAuthor().getProfileImageUrl().replace("normal", "400x400")).into(vh.profileImage);
        vh.authorName.setText(tweet.getAuthor().getName());
        vh.authorUsername.setText("@" + tweet.getAuthor().getUsername() + " • ");
        vh.timestamp.setText(RelativeTimestamp.get(tweet.getCreation()));
        vh.retweetButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getRetweetCount() + tweet.getMetrics().getQuoteCount()));
        vh.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
        vh.itemView.setTag(tweet);

        if (tweet.getRetweeter() != null) {
            vh.tweetAction.setVisibility(View.VISIBLE);
            vh.actionLabel.setText(holder.itemView.getContext().getString(R.string.tweet_retweeted_by_label, tweet.getRetweeter().getName()));

            vh.tweetAction.setOnClickListener(l -> {
                final Context context = holder.itemView.getContext();
                final Intent intent = new Intent(context, ProfileActivity.class);
                intent.putExtra("user_id", tweet.getRetweeter().getId());
                intent.putExtra("is_id", true);
                context.startActivity(intent);
            });
        }
        else {
            vh.tweetAction.setVisibility(View.GONE);
        }

        vh.likeButton.setChipIconResource(tweet.isLiked() ? R.drawable.ic_like_filled : R.drawable.ic_like);

        final Context context = holder.itemView.getContext();
        final Intent profileIntent = new Intent(context, ProfileActivity.class).putExtra("user_id", tweet.getAuthor().getId()).putExtra("is_id", true);

        holder.itemView.setOnClickListener(v -> {
            CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
            intent.launchUrl(context, Uri.parse("https://twitter.com/" + tweet.getAuthor().getUsername() + "/status/" + tweet.getId()));
        });

        vh.profileImage.setOnClickListener(v -> context.startActivity(profileIntent));

        vh.replyButton.setOnClickListener(v -> {
            final ReplyBottomSheet replyBottomSheet = new ReplyBottomSheet();
            final Bundle bundle = new Bundle();
            bundle.putSerializable("tweet", tweet);
            replyBottomSheet.setArguments(bundle);

            replyBottomSheet.show(((AppCompatActivity) holder.itemView.getContext()).getSupportFragmentManager(), ReplyBottomSheet.TAG);
        });

        vh.likeButton.setOnClickListener(l -> {
            final String userId = context.getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("id", null);
            if (!tweet.isLiked()) {
                JSONObject body = new JSONObject();
                try {
                    body.put("tweet_id", tweet.getId());
                }
                catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(context, "Failed to like tweet", Toast.LENGTH_SHORT).show();
                    return;
                }

                JsonNetworkRequest.postObject(context, "https://api.twitter.com/2/users/" + userId + "/likes", body, response -> {
                    if (!response.isPresent()) {
                        Toast.makeText(context, "Failed to like tweet", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        vh.likeButton.setChipIconResource(R.drawable.ic_like_filled);
                        tweet.getMetrics().incrementLikeCount();
                        tweet.setLiked(true);
                        vh.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
                    }
                });
            }
            else {
                JsonNetworkRequest.sendDelete(context, "https://api.twitter.com/2/users/" + userId + "/likes/" + tweet.getId(), response -> {
                    if (!response.isPresent()) {
                        Toast.makeText(context, "Failed to unlike tweet", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        vh.likeButton.setChipIconResource(R.drawable.ic_like);
                        tweet.getMetrics().decrementLikeCount();
                        tweet.setLiked(false);
                        vh.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
                    }
                });
            }

            // update the cached tweet in the cached timeline
            final String timelineCache = DataCache.get(context, "home_timeline.json");

            try {
                JSONArray cachedTimeline = new JSONArray(timelineCache);
                for (int i = 0; i < cachedTimeline.length(); i++) {
                    JSONObject cachedTweet = cachedTimeline.getJSONObject(i);
                    if (cachedTweet.getString("id_str").equals(tweet.getId())) {
                        cachedTweet.put("favorited", tweet.isLiked());
                        cachedTweet.put("favorite_count", tweet.getMetrics().getLikeCount());
                        break;
                    }
                }

                DataCache.set(context, "home_timeline.json", cachedTimeline.toString());
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        if (tweets.get(position) instanceof Profile) {
            return -1;
        }

        final Tweet tweet = (Tweet) tweets.get(position);
        return tweet.getMedia().size() > 0 ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void addToBeginning(@NonNull JSONArray data) {
        for (int i = data.length() - 1; i > -1; i--) {
            tweets.add(1, new Tweet(data.optJSONObject(i)));
        }
        notifyItemRangeInserted(1, data.length());
    }

    public void addToEnd(@NonNull JSONArray data) {
        for (int i = 0; i < data.length(); i++) {
            tweets.add(new Tweet(data.optJSONObject(i)));
        }
        notifyItemRangeInserted(tweets.size() - data.length(), data.length());
    }
}
