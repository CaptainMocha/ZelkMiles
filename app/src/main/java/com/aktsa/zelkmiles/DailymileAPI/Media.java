package com.aktsa.zelkmiles.DailymileAPI;

/**
 * Created by cheek on 8/14/2015.
 */
import com.google.gson.annotations.Expose;

public class Media {

    @Expose
    private Content content;

    /**
     *
     * @return
     * The content
     */
    public Content getContent() {
        return content;
    }

    /**
     *
     * @param content
     * The content
     */
    public void setContent(Content content) {
        this.content = content;
    }

}