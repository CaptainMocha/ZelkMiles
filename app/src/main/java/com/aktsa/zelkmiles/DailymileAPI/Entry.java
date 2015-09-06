package com.aktsa.zelkmiles.DailymileAPI;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

public class Entry {

    @Expose
    private Integer id;
    @Expose
    private String url;
    @Expose
    private String at;
    @Expose
    private String message;
    @Expose
    private List<Comment> comments = new ArrayList<Comment>();
    @Expose
    private List<Like> likes = new ArrayList<Like>();
    @Expose
    private Location location;
    @Expose
    private User user;
    @Expose
    private Workout workout;
    @Expose
    private List<Medium> media = new ArrayList<Medium>();

    /**
     * @return The id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id The id
     */
    public void setId(Integer id) {
        this.id = id;
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
     * @return The at
     */
    public String getAt() {
        return at;
    }

    /**
     * @param at The at
     */
    public void setAt(String at) {
        this.at = at;
    }

    /**
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message The message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return The comments
     */
    public List<Comment> getComments() {
        return comments;
    }

    /**
     * @param comments The comments
     */
    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    /**
     * @return The likes
     */
    public List<Like> getLikes() {
        return likes;
    }

    /**
     * @param likes The likes
     */
    public void setLikes(List<Like> likes) {
        this.likes = likes;
    }

    /**
     * @return The location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * @param location The location
     */
    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * @return The user
     */
    public User getUser() {
        return user;
    }

    /**
     * @param user The user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * @return The workout
     */
    public Workout getWorkout() {
        return workout;
    }

    /**
     * @param workout The workout
     */
    public void setWorkout(Workout workout) {
        this.workout = workout;
    }

    /**
     * @return The media
     */
    public List<Medium> getMedia() {
        return media;
    }

    /**
     * @param media The media
     */
    public void setMedia(List<Medium> media) {
        this.media = media;
    }

}