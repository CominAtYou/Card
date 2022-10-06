package com.cominatyou.card.data;

import org.json.JSONArray;

public class Profile {
    private final String name;
    private final String handle;
    private final String id;
    private final String description;
    private final String url;
    private final String displayUrl;
    private final JSONArray descriptionUrls;
    private final String location;
    private final String profileImageUrl;
    private final int followersCount;
    private final int followingCount;

    public Profile(String name, String handle, String id, String description, String url, String displayUrl, JSONArray descriptionUrls, String location, String profileImageUrl, int followersCount, int followingCount) {
        this.name = name;
        this.handle = handle;
        this.id = id;
        this.description = description;
        this.url = url;
        this.displayUrl = displayUrl;
        this.descriptionUrls = descriptionUrls;
        this.location = location;
        this.profileImageUrl = profileImageUrl;
        this.followersCount = followersCount;
        this.followingCount = followingCount;
    }

    public String getName() {
        return name;
    }

    public String getHandle() {
        return handle;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public String getDisplayUrl() {
        return displayUrl;
    }

    public JSONArray getDescriptionUrls() {
        return descriptionUrls;
    }

    public String getLocation() {
        return location;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public int getFollowerCount() {
        return followersCount;
    }

    public int getFollowingCount() {
        return followingCount;
    }
}
