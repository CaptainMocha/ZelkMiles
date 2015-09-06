package com.aktsa.zelkmiles.DailymileAPI;

import com.google.gson.annotations.Expose;

public class Location {

    @Expose
    private String name;

    /**
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name The name
     */
    public void setName(String name) {
        this.name = name;
    }

}