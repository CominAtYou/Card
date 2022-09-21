package com.cominatyou.card.data;

import com.cominatyou.card.auth.Auth;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.Objects;

public class Tweet {
    private final String id;
    private final Instant createdAt;
    private final Metrics metrics;
    private final String text;
    private final Author author;
    private final JSONArray urls;

    public Tweet(JSONObject tweet, Metrics metrics, Author author) {
        this.id = tweet.optString("id");
        this.createdAt = Instant.parse(tweet.optString("created_at"));
        this.metrics = metrics;
        this.text = tweet.optString("text");
        this.author = author;
        if (tweet.has("entities") && tweet.optJSONObject("entities").has("urls")) {
            this.urls = tweet.optJSONObject("entities").optJSONArray("urls");
        } else {
            this.urls = new JSONArray();
        }
    }

    public String getId() {
        return id;
    }

    public Instant getCreation() {
        return createdAt;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public String getText() {
        return text;
    }

    public Author getAuthor() {
        return author;
    }

    public JSONArray getUrls() {
        return urls;
    }

    public static class Metrics {
        private final int retweetCount;
        private final int replyCount;
        private final int likeCount;
        private final int quoteCount;

        public Metrics(JSONObject metrics) {
            this.retweetCount = metrics.optInt("retweet_count");
            this.replyCount = metrics.optInt("reply_count");
            this.likeCount = metrics.optInt("like_count");
            this.quoteCount = metrics.optInt("quote_count");
        }

        public int getRetweetCount() {
            return retweetCount;
        }

        public int getReplyCount() {
            return replyCount;
        }

        public int getLikeCount() {
            return likeCount;
        }

        public int getQuoteCount() {
            return quoteCount;
        }
    }

    public static class Author {
        private final String id;
        private final String name;
        private final String username;
        private final String profileImageUrl;

        public Author(JSONObject user) {
            this.id = user.optString("id");
            this.name = user.optString("name");
            this.username = user.optString("username");
            this.profileImageUrl = user.optString("profile_image_url");
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getProfileImageUrl() {
            return profileImageUrl;
        }
    }
}
