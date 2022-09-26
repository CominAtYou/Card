package com.cominatyou.card.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cominatyou.card.JsonNetworkRequest;
import com.cominatyou.card.ProfileActivity;
import com.cominatyou.card.R;
import com.cominatyou.card.activityhelpers.ReplyBottomSheet;
import com.cominatyou.card.data.Tweet;
import com.cominatyou.card.util.LinkOnTouchListener;
import com.cominatyou.card.util.LinkUtil;
import com.cominatyou.card.util.NumberUtil;
import com.cominatyou.card.util.RelativeTimestamp;
import com.google.android.material.chip.Chip;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import org.apache.commons.text.StringEscapeUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TimelineAdapter extends RecyclerView.Adapter<TimelineAdapter.TimelineTweetHolder> {
    private final List<Tweet> tweets = new ArrayList<>();
    private final FragmentManager fragmentManager;

    public TimelineAdapter(JSONArray response, FragmentManager fragmentManager) {
        for (int i = 0; i < response.length(); i++) {
            final JSONObject tweet = response.optJSONObject(i);
            tweets.add(new Tweet(tweet));
        }

        this.fragmentManager = fragmentManager;
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
            final String url = media.getPreviewImageUrl() == null ? media.getUrl() : media.getPreviewImageUrl();
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

        holder.authorUsername.setText("@" + tweet.getAuthor().getUsername() + " • ");
        holder.timestamp.setText(RelativeTimestamp.get(creationDate));
//        holder.replyButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getReplyCount()));
        holder.retweetButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getRetweetCount() + tweet.getMetrics().getQuoteCount()));
        holder.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
        holder.itemView.setTag(tweet);

        holder.likeButton.setChipIconResource(tweet.isLiked() ? R.drawable.ic_like_filled : R.drawable.ic_like);

        final Context context = holder.itemView.getContext();

        final Intent profileIntent = new Intent(context, ProfileActivity.class).putExtra("user_id", tweet.getAuthor().getId()).putExtra("is_id", true);
        holder.itemView.setOnClickListener(v -> {
            CustomTabsIntent intent = new CustomTabsIntent.Builder().build();
            intent.launchUrl(context, Uri.parse("https://twitter.com/" + tweet.getAuthor().getUsername() + "/status/" + tweet.getId()));
        });
        holder.profileImage.setOnClickListener(v -> context.startActivity(profileIntent));

        holder.replyButton.setOnClickListener(v -> {
            final ReplyBottomSheet replyBottomSheet = new ReplyBottomSheet();
            final Bundle bundle = new Bundle();
            bundle.putSerializable("tweet", tweet);
            replyBottomSheet.setArguments(bundle);

            replyBottomSheet.show(fragmentManager, ReplyBottomSheet.TAG);
        });

        holder.likeButton.setOnClickListener(l -> {
            if (!tweet.isLiked()) {
                final String userId = context.getSharedPreferences("user_data", Context.MODE_PRIVATE).getString("id", null);
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
                    if (response == null) {
                        Toast.makeText(context, "Failed to like tweet", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        holder.likeButton.setChipIconResource(R.drawable.ic_like_filled);
                        tweet.getMetrics().incrementLikeCount();
                        tweet.setLiked(true);
                        holder.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
                    }
                });
            }
            else {
                JsonNetworkRequest.sendDelete(context, "https://api.twitter.com/2/users/" + tweet.getAuthor().getId() + "/likes/" + tweet.getId(), response -> {
                    if (response == null) {
                        Toast.makeText(context, "Failed to unlike tweet", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        holder.likeButton.setChipIconResource(R.drawable.ic_like);
                        tweet.getMetrics().decrementLikeCount();
                        tweet.setLiked(false);
                        holder.likeButton.setText(NumberUtil.formatNumber(tweet.getMetrics().getLikeCount()));
                    }
                });
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return tweets.get(position).getMedia().size() > 0 ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return tweets.size();
    }

    public void addToBeginning(@NonNull JSONArray data) {
        for (int i = data.length() - 1; i > -1; i--) {
            tweets.add(0, new Tweet(data.optJSONObject(i)));
        }
        notifyItemRangeInserted(0, data.length());
    }

    public Tweet getItemAtIndex(int index) {
        return tweets.get(index);
    }
}
