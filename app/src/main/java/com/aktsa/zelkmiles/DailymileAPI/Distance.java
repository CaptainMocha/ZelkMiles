package com.aktsa.zelkmiles.DailymileAPI;

import com.google.gson.annotations.Expose;

public class Distance {

    @Expose
    private Float value;
    @Expose
    private String units;

    /**
     * @return The value
     */
    public Float getValue() {
        return value;
    }

    /**
     * @param value The value
     */
    public void setValue(Float value) {
        this.value = value;
    }

    /**
     * @return The units
     */
    public String getUnits() {
        return units;
    }

    /**
     * @param units The units
     */
    public void setUnits(String units) {
        this.units = units;
    }

}