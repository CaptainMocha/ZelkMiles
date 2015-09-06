package com.aktsa.zelkmiles.DailymileAPI;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;

public class Stream {

    @Expose
    private List<Entry> entries = new ArrayList<Entry>();

    /**
     * @return The entries
     */
    public List<Entry> getEntries() {
        return entries;
    }

    /**
     * @param entries The entries
     */
    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

}