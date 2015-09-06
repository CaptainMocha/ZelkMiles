package com.aktsa.zelkmiles;

/**
 * Created by cheek on 8/14/2015.
 */
public class Constants {

    public static final String API_ENDPOINT = "https://api.dailymile.com";

    public static final String SHARED_PREFS = "ZelkMilesPreferences";

    public static final String DM_CLIENT_ID = "OmVBiEm3Eef44VxDYjc7gYQfjNiVTtJzhy3PEIb3";
    public static final String IM_CLIENT_ID = "a10f8f0ffde9053";
    public static final String IM_CLIENT_SECRET = "ade79af9b3144cf3e20068db84b255977ae069a2";
    public static final String CALLBACK_URL = "http://localhost";

    public static final boolean LOGGING = false;

    public static String getClientAuth() {
        return "Client-ID " + IM_CLIENT_ID;
    }

}
