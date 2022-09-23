package com.cominatyou.card.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Instant;
import java.util.ArrayList;

public class Tweet {
    private final String id;
    private final Instant createdAt;
    private final Metrics metrics;
    private final String text;
    private final Author author;
    private final ArrayList<Url> urls = new ArrayList<>();
    private final ArrayList<Media> media;

    public Tweet(JSONObject tweet, Metrics metrics, Author author, ArrayList<Media> media) {
        this.id = tweet.optString("id");
        this.createdAt = Instant.parse(tweet.optString("created_at"));
        this.metrics = metrics;
        this.author = author;
        this.media = media;

        String tweetText = tweet.optString("text");
        if (tweet.has("entities") && tweet.optJSONObject("entities").has("urls")) {
            for (int i = 0; i < tweet.optJSONObject("entities").optJSONArray("urls").length(); i++) {
                final JSONObject url = tweet.optJSONObject("entities").optJSONArray("urls").optJSONObject(i);

                if (url.has("media_key")) {
                    tweetText = tweetText.replace(url.optString("url"), "");
                    continue;
                }

                this.urls.add(new Url(url));
            }
        }

        this.text = tweetText;
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

    public ArrayList<Url> getUrls() {
        return urls;
    }

    public ArrayList<Media> getMedia() {
        return media;
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

    public static class Url {
        private final int startIndex;
        private final int endIndex;
        private final String url;
        private final String expandedUrl;
        private final String displayUrl;

        public Url(JSONObject url) {
            this.startIndex = url.optInt("start");
            this.endIndex = url.optInt("end");
            this.url = url.optString("url");
            this.expandedUrl = url.optString("expanded_url");
            this.displayUrl = url.optString("display_url");
        }

        public int getStartIndex() {
            return startIndex;
        }

        public int getEndIndex() {
            return endIndex;
        }

        public String getUrl() {
            return url;
        }

        public String getExpandedUrl() {
            return expandedUrl;
        }

        public String getDisplayUrl() {
            return displayUrl;
        }
    }

    public static class Media {
        private final String url;
        private final String previewImageUrl;
        private final String type;
        private final int width;
        private final int height;
        private final String mediaKey;
        private final int durationMs;

        public Media(JSONObject media) {
            this.url = media.optString("url");
            this.previewImageUrl = media.optString("preview_image_url");
            this.type = media.optString("type");
            this.width = media.optInt("width");
            this.height = media.optInt("height");
            this.mediaKey = media.optString("media_key");
            this.durationMs = media.optInt("duration_ms");
        }

        public String getUrl() {
            return url;
        }

        public String getPreviewImageUrl() {
            return previewImageUrl;
        }

        public String getType() {
            return type;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getMediaKey() {
            return mediaKey;
        }

        public int getDurationMs() {
            return durationMs;
        }
    }
}
