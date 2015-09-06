package com.aktsa.zelkmiles.DailymileAPI;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Workout {

    @SerializedName("activity_type")
    @Expose
    private String activity_type;
    @Expose
    private Distance distance;
    @Expose
    private String felt;
    @Expose
    private Integer duration;
    @Expose
    private String title;
    @Expose
    private Integer calories;
    @Expose
    private Integer route_id;
    @Expose
    private String completed_at;

    /**
     * @return The activity_type
     */
    public String getActivity_type() {
        return activity_type;
    }

    /**
     * @param activity_type The activity_type
     */
    public void setActivity_type(String activity_type) {
        this.activity_type = activity_type;
    }

    /**
     * @return The felt
     */
    public String getFelt() {
        return felt;
    }

    /**
     * @param felt The felt
     */
    public void setFelt(String felt) {
        this.felt = felt;
    }

    /**
     * @return The duration
     */
    public Integer getDuration() {
        return duration;
    }

    /**
     * @param duration The duration
     */
    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    /**
     * @return The distance
     */
    public Distance getDistance() {
        return distance;
    }

    /**
     * @param distance The distance
     */
    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The calories
     */
    public Integer getCalories() {
        return calories;
    }

    /**
     * @param calories The calories
     */
    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    /**
     * @return The route_id
     */
    public Integer getRoute_id() {
        return route_id;
    }

    /**
     * @param route_id The route_id
     */
    public void setRoute_id(Integer route_id) {
        this.route_id = route_id;
    }

    /**
     * @return The completed_at
     */
    public String getCompleted_at() {
        return completed_at;
    }

    /**
     * @param completed_at The completed_at
     */
    public void setCompleted_at(String completed_at) {
        this.completed_at = completed_at;
    }

}