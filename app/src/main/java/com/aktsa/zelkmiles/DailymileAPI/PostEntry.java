package com.aktsa.zelkmiles.DailymileAPI;

import com.google.gson.annotations.Expose;

/**
 * Created by cheek on 8/18/2015.
 */
public class PostEntry {

    @Expose
    private String message;
    @Expose
    private Float lat;
    @Expose
    private Float lon;
    @Expose
    private Workout workout;
    @Expose
    private Content media;

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
     * @return The lat
     */
    public Float getLat() {
        return lat;
    }

    /**
     * @param lat The lat
     */
    public void setLat(Float lat) {
        this.lat = lat;
    }

    /**
     * @return The lon
     */
    public Float getLon() {
        return lon;
    }

    /**
     * @param lon The lon
     */
    public void setLon(Float lon) {
        this.lon = lon;
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
    public Content getMedia() {
        return media;
    }

    /**
     * @param media The media
     */
    public void setMedia(Content media) {
        this.media = media;
    }
}