package com.cominatyou.card.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.recyclerview.widget.RecyclerView;

import com.cominatyou.card.ProfileActivity;
import com.cominatyou.card.R;
import com.cominatyou.card.data.Tweet;
import com.cominatyou.card.util.LinkOnTouchListener;
import com.cominatyou.card.util.LinkUtil;
import com.cominatyou.card.util.NumberUtil;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineTweetHolder> {
    private final List<Tweet> tweets = new ArrayList<>();

    public TimelineAdapter(JSONObject response) {
        final JSONArray data = response.optJSONArray("data");

        for (int i = 0; i < data.length(); i++) {
            final JSONObject tweet = data.optJSONObject(i);
            final Tweet.Metrics metrics = new Tweet.Metrics(tweet.optJSONObject("public_metrics"));
            final String authorId = tweet.optString("author_id");

            // find the tweet author object in the "users" array inside the "includes" object, based on their id
            final JSONObject includes = response.optJSONObject("includes");
            final JSONArray users = includes.optJSONArray("users");

            for (int j = 0; j < users.length(); j++) {
                final JSONObject user = users.optJSONObject(j);
                final String id = user.optString("id");

                if (id.equals(authorId)) {
                    final Tweet.Author author = new Tweet.Author(user);
                    tweets.add(new Tweet(tweet, metrics, author, getMedia(response, tweet)));
                    break;
                }
            }
        }
    }

    public static class TimelineTweetHolder extends RecyclerView.ViewHolder {
        private final TextView tweetText;
        private final ShapeableImageView profileImage;
        private final TextView authorName;
        private final TextView authorUsername;
        private final TextView timestamp;
        private final Chip retweetButton;
        private final Chip replyButton;
        private final Chip likeButton;

        public TimelineTweetHolder(View itemView) {
            super(itemView);

            tweetText = itemView.findViewById(R.id.tweet_text_content);
            profileImage = itemView.findViewById(R.id.tweet_author_avatar);
            authorName = itemView.findViewById(R.id.tweet_author_name);
            authorUsername = itemView.findViewById(R.id.tweet_author_handle);
            timestamp = itemView.findViewById(R.id.tweet_timestamp);
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

    @NonNull
    @Override
    public TimelineTweetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final View tweetView = inflater.inflate(viewType == 0 ? R.layout.tweet_text_only : R.layout.tweet_single_image, parent, false);
        return viewType == 0 ? new TimelineTweetHolder(tweetView) : new SingleMediaItemTimelineTweetHolder(tweetView);
    }

    @SuppressLint({"ClickableViewAccessibility", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull TimelineTweetHolder holder, int position) {
        final Tweet tweet = tweets.get(position);

        // Tweet content is HTML encoded. I don't know why.
        final String unescapedContent = StringEscapeUtils.unescapeHtml4(tweet.getText());

        if (holder instanceof SingleMediaItemTimelineTweetHolder) {
            final SingleMediaItemTimelineTweetHolder singleMediaItemTimelineTweetHolder = (SingleMediaItemTimelineTweetHolder) holder;
            final Tweet.Media media = tweet.getMedia().get(0);
            final String url = media.getPreviewImageUrl().isEmpty() ? media.getUrl() : media.getPreviewImageUrl();
            Picasso.get().load(url).into(singleMediaItemTimelineTweetHolder.imageView);

            if (tweet.getText().isEmpty()) holder.tweetText.setVisibility(View.GONE);
            else holder.tweetText.setVisibility(View.VISIBLE); // if this is a recycled view that had a previously hidden TextView, un-hide it
        }

        if (tweet.getUrls().size() > 0) {
            holder.tweetText.setText(LinkUtil.addHyperlinks(tweet.getUrls(), unescapedContent));
            holder.tweetText.setOnTouchListener(LinkOnTouchListener.getListener());
        }
        else {
            holder.tweetText.setText(unescapedContent);
        }

        Picasso.get().load(tweet.getAuthor().getProfileImageUrl().replace("normal", "400x400")).into(holder.profileImage);
        holder.authorName.setText(tweet.getAuthor().getName());

        // format the creation date as either "1s", "1m", "1h", or "1d"
        Instant creationDate = tweet.getCreation();
        long seconds = Instant.now().getEpochSecond() - creationDate.getEpochSecond();
        String creationDateText;
        if (seconds < 60) {
            creationDateText = seconds + "s";
        } else if (seconds < 3600) {
            creationDateText = seconds / 60 + "m";
        } else if (seconds < 86400) {
            creationDateText = seconds / 3600 + "h";
        } else {
            creationDateText = seconds / 86400 + "d";
        }

        holder.authorUsername.setText("@" + tweet.getAuthor().getUsername() + " · ");
        holder.timestamp.setText(creationDateText);
        holder.replyButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getReplyCount()));
        holder.retweetButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getRetweetCount() + tweet.getMetrics().getQuoteCount()));
        holder.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
        holder.itemView.setTag(tweet.getId());

        final Context context = holder.itemView.getContext();

        final Intent profileIntent = new Intent(context, ProfileActivity.class).putExtra("user_id", tweet.getAuthor().getId()).putExtra("is_id", true);
        holder.itemView.setOnClickListener(v -> {
            CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
            intent.launchUrl(context, Uri.parse("https://twitter.com/" + tweet.getAuthor().getUsername() + "/status/" + tweet.getId()));
        });
        holder.profileImage.setOnClickListener(v -> context.startActivity(profileIntent));
    }

    @Override
    public int getItemViewType(int position) {
        return tweets.get(position).getMedia().size() > 0 ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void addToBeginning(@NonNull JSONObject response) {
        final JSONArray data = response.optJSONArray("data");

        for (int i = data.length() - 1; i > -1; i--) {
            final JSONObject tweet = data.optJSONObject(i);
            final Tweet.Metrics metrics = new Tweet.Metrics(tweet.optJSONObject("public_metrics"));
            final String authorId = tweet.optString("author_id");

            // find the tweet author object in the "users" array inside the "includes" object, based on their id
            final JSONObject includes = response.optJSONObject("includes");
            final JSONArray users = includes.optJSONArray("users");

            for (int j = 0; j < users.length(); j++) {
                final JSONObject user = users.optJSONObject(j);
                final String id = user.optString("id");

                if (id.equals(authorId)) {
                    final Tweet.Author author = new Tweet.Author(user);
                    tweets.add(0, new Tweet(tweet, metrics, author, getMedia(response, tweet)));
                    break;
                }
            }

            notifyItemRangeInserted(0, data.length());
        }
    }

    private ArrayList<Tweet.Media> getMedia(JSONObject response, JSONObject tweet) {
        final ArrayList<Tweet.Media> media = new ArrayList<>();

        if (tweet.has("attachments") && tweet.optJSONObject("attachments").has("media_keys")) {
            final JSONArray mediaKeys = tweet.optJSONObject("attachments").optJSONArray("media_keys");
            final JSONObject includes = response.optJSONObject("includes");
            final JSONArray mediaItems = includes.optJSONArray("media");

            for (int j = 0; j < mediaItems.length(); j++) {
                final JSONObject mediaObject = mediaItems.optJSONObject(j);
                final String mediaKey = mediaObject.optString("media_key");

                for (int k = 0; k < mediaKeys.length(); k++) {
                    final String mediaKeyFromTweet = mediaKeys.optString(k);

                    if (mediaKey.equals(mediaKeyFromTweet)) {
                        media.add(new Tweet.Media(mediaObject));
                        break;
                    }
                }
            }
        }

        return media;
    }
}
