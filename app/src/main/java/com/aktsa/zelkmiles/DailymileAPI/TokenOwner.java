package com.aktsa.zelkmiles.DailymileAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TokenOwner {

    @Expose
    private String location;
    @SerializedName("time_zone")
    @Expose
    private String timeZone;
    @Expose
    private String url;
    @SerializedName("display_name")
    @Expose
    private String displayName;
    @Expose
    private String username;
    @SerializedName("photo_url")
    @Expose
    private String photoUrl;

    /**
     * @return The location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location The location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return The timeZone
     */
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * @param timeZone The time_zone
     */
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * @return The url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param url The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * @return The displayName
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * @param displayName The display_name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * @return The username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username The username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return The photoUrl
     */
    public String getPhotoUrl() {
        return photoUrl;
    }

    /**
     * @param photoUrl The photo_url
     */
    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }
}