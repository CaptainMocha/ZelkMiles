package com.aktsa.zelkmiles;

import android.util.Log;

/**
 * Created by cheek on 8/20/2015.
 */
public class aLog {
    public static void w (String TAG, String msg){
        if(Constants.LOGGING) {
            if (TAG != null && msg != null)
                Log.w(TAG, msg);
        }
    }

}