package com.aktsa.zelkmiles.DailymileAPI;

import com.google.gson.annotations.Expose;

public class Medium {

    @Expose
    private Preview preview;
    @Expose
    private Content content;

    /**
     * @return The preview
     */
    public Preview getPreview() {
        return preview;
    }

    /**
     * @param preview The preview
     */
    public void setPreview(Preview preview) {
        this.preview = preview;
    }

    /**
     * @return The content
     */
    public Content getContent() {
        return content;
    }

    /**
     * @param content The content
     */
    public void setContent(Content content) {
        this.content = content;
    }

}