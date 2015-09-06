package com.aktsa.zelkmiles;

/**
 * Created by cheek on 8/14/2015.
 */
public class StreamItem {
    String title;
    String info;
    String extraInfo;
    String message;
    String avatarUrl;
    String timeSince;
    String mediaUrl;
    Integer id;
    String username;
    Integer width;
    Integer height;

    StreamItem(String title, String info, String extraInfo, String message, String avatarUrl, String timeSince, String mediaUrl, Integer id, String username, Integer width, Integer height) {
        this.title = title;
        this.info = info;
        this.extraInfo = extraInfo;
        this.message = message;
        this.avatarUrl = avatarUrl;
        this.timeSince = timeSince;
        this.mediaUrl = mediaUrl;
        this.id = id;
        this.username = username;
        this.width = width;
        this.height = height;
    }
}
