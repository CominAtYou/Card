package com.cominatyou.card.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.cominatyou.card.ProfileActivity;
import com.cominatyou.card.data.Tweet;
import com.cominatyou.card.util.NumberUtil;
import com.cominatyou.card.R;
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
                    tweets.add(new Tweet(tweet, metrics, author));
                    break;
                }
            }
        }
    }

    public static class TimelineTweetHolder extends RecyclerView.ViewHolder {
        private final TextView tweetText;
        private final ShapeableImageView profileImage;
        private final TextView authorName;
        private final TextView authorSubtext;
        private final Chip retweetButton;
        private final Chip replyButton;
        private final Chip likeButton;

        public TimelineTweetHolder(View itemView) {
            super(itemView);

            tweetText = itemView.findViewById(R.id.tweet_text_content);
            profileImage = itemView.findViewById(R.id.text_only_tweet_profile_picture);
            authorName = itemView.findViewById(R.id.text_only_author_name);
            authorSubtext = itemView.findViewById(R.id.text_only_username_timestamp);
            retweetButton = itemView.findViewById(R.id.text_only_tweet_retweet_chip);
            replyButton = itemView.findViewById(R.id.text_only_tweet_reply_chip);
            likeButton = itemView.findViewById(R.id.text_only_tweet_like_chip);
        }
    }

    @NonNull
    @Override
    public TimelineTweetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        final LayoutInflater inflater = LayoutInflater.from(context);

        final View tweetView = inflater.inflate(R.layout.tweet_text_only, parent, false);
        return new TimelineTweetHolder(tweetView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TimelineTweetHolder holder, int position) {
        final Tweet tweet = tweets.get(position);

        // Tweet content is HTML encoded. I don't know why.
        holder.tweetText.setText(StringEscapeUtils.unescapeHtml4(tweet.getText()));
        Picasso.get().load(tweet.getAuthor().getProfileImageUrl()).into(holder.profileImage);
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

        holder.authorSubtext.setText("@" + tweet.getAuthor().getUsername() + " · " + creationDateText);
        holder.replyButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getReplyCount()));
        holder.retweetButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getRetweetCount() + tweet.getMetrics().getQuoteCount()));
        holder.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
        holder.itemView.setTag(tweet.getId());

        final Context context = holder.itemView.getContext();

        final Intent intent = new Intent(context, ProfileActivity.class).putExtra("user_id", tweet.getAuthor().getId()).putExtra("is_id", true);
        holder.itemView.setOnClickListener(v -> Toast.makeText(context, (String) holder.itemView.getTag(), Toast.LENGTH_LONG).show());
        holder.profileImage.setOnClickListener(v -> context.startActivity(intent));
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }
}
