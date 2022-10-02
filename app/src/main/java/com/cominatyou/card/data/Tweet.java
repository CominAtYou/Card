package com.cominatyou.card.data;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Tweet implements Serializable {
    private final String id;
    private final Instant createdAt;
    private final Metrics metrics;
    private final String text;
    private final Author author;
    private final Author retweeter;
    private boolean retweeted;
    private boolean liked;
    private final ArrayList<Url> urls = new ArrayList<>();
    private final ArrayList<Media> media = new ArrayList<>();

    public Tweet(JSONObject tweet) {
        this.id = tweet.optString("id_str");
        Author retweeterTmp = null;

        if (tweet.has("retweeted_status")) {
            final JSONObject retweetedStatus = tweet.optJSONObject("retweeted_status");
            final JSONObject retweetedBy = tweet.optJSONObject("user");
            retweeterTmp = new Author(retweetedBy.optString("id_str"), retweetedBy.optString("name"), retweetedBy.optString("screen_name"), retweetedBy.optString("profile_image_url_https"));
            tweet = retweetedStatus;
        }

        this.retweeter = retweeterTmp;
        DateFormat df = new SimpleDateFormat("EEE MMM dd hh:mm:ss +0000 yyyy", Locale.US);
        df.setTimeZone(TimeZone.getTimeZone("UTC"));

        Instant parsedCreatedAt;
        try {
            final Date date = df.parse(tweet.optString("created_at"));
            parsedCreatedAt = date.toInstant();
        }
        catch (Exception e) {
            parsedCreatedAt = Instant.now();
            e.printStackTrace();
        }

        // I don't know why I have to do it like this - I just do or the compiler complains
        this.createdAt = parsedCreatedAt;
        this.metrics = new Metrics(tweet.optInt("retweet_count"), 0, tweet.optInt("favorite_count"), 0);

        final JSONObject user = tweet.optJSONObject("user");
        this.author = new Author(user.optString("id_str"), user.optString("name"), user.optString("screen_name"), user.optString("profile_image_url_https"));

        this.liked = tweet.optBoolean("favorited");
        this.retweeted = tweet.optBoolean("retweeted");

        final JSONObject entities = tweet.optJSONObject("entities");
        final JSONObject extendedEntities = tweet.optJSONObject("extended_entities");
        String text = tweet.optString("full_text");

        if (extendedEntities != null) {
            final JSONArray extendedMedia = extendedEntities.optJSONArray("media");
            for (int j = 0; j < extendedMedia.length(); j++) {
                final JSONObject extendedMediaObject = extendedMedia.optJSONObject(j);

                final String mediaUrl = extendedMediaObject.optString("url");
                text = text.replace(mediaUrl, "");
                // find the media object in the video_info array with the highest bitrate value

                if (extendedMediaObject.optString("type").equals("video")) {
                    final JSONObject videoInfo = extendedMediaObject.optJSONObject("video_info");
                    final JSONArray variants = videoInfo.optJSONArray("variants");
                    JSONObject highestBitrate = null;
                    for (int l = 0; l < variants.length(); l++) {
                        final JSONObject variant = variants.optJSONObject(l);
                        if (highestBitrate == null || variant.optInt("bitrate") > highestBitrate.optInt("bitrate")) {
                            highestBitrate = variant;
                        }
                    }
                    media.add(new Media(highestBitrate.optString("url"), extendedMediaObject.optString("media_url_https"), extendedMediaObject.optString("type"), videoInfo.optInt("duration_millis")));
                }
                else {
                    media.add(new Media(extendedMediaObject.optString("media_url_https"), null, extendedMediaObject.optString("type"), 0));
                }
            }
        }

        this.text = text;

        for (int j = 0; j < entities.optJSONArray("urls").length(); j++) {
            final JSONObject url = entities.optJSONArray("urls").optJSONObject(j);

            urls.add(new Url(url.optString("url"), url.optString("display_url")));
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

    public Author getRetweeter() {
        return retweeter;
    }

    public ArrayList<Url> getUrls() {
        return urls;
    }

    public ArrayList<Media> getMedia() {
        return media;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public static class Metrics implements Serializable {
        private int retweetCount, likeCount;
        private final int replyCount, quoteCount;
        public Metrics(int retweetCount, int replyCount, int likeCount, int quoteCount) {
            this.retweetCount = retweetCount;
            this.replyCount = replyCount;
            this.likeCount = likeCount;
            this.quoteCount = quoteCount;
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

        public void incrementRetweetCount() {
            this.retweetCount++;
        }

        public void incrementLikeCount() {
            this.likeCount++;
        }

        public void decrementRetweetCount() {
            this.retweetCount--;
        }

        public void decrementLikeCount() {
            this.likeCount--;
        }
    }

    public static class Author implements Serializable {
        private final String id;
        private final String name;
        private final String username;
        private final String profileImageUrl;

        public Author(String id, String name, String username, String profileImageUrl) {
            this.id = id;
            this.name = name;
            this.username = username;
            this.profileImageUrl = profileImageUrl.replace("normal", "400x400");
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

    public static class Url implements Serializable {
        private final String url;
        private final String displayUrl;

        public Url(String url, String displayUrl) {
            this.url = url;
            this.displayUrl = displayUrl;
        }

        public String getUrl() {
            return url;
        }

        public String getDisplayUrl() {
            return displayUrl;
        }
    }

    public static class Media implements Serializable {
        private final String url;
        private final String previewImageUrl;
        private final String type;
        private final int durationMs;

        public Media(String url, String previewImageUrl, String type, int durationMs) {
            this.url = url;
            this.previewImageUrl = previewImageUrl;
            this.type = type;
            this.durationMs = durationMs;
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

        public int getDurationMs() {
            return durationMs;
        }
    }
}
