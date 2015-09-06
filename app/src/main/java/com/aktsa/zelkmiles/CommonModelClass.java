package com.aktsa.zelkmiles;

import android.app.Activity;

/**
 * Created by cheek on 8/22/2015.
 */
public class CommonModelClass {
    public static CommonModelClass singletonObject;
    /**
     * A private Constructor prevents any other class from instantiating.
     */

    private Activity baseActivity;

    public CommonModelClass() {
        //   Optional Code
    }

    public static synchronized CommonModelClass getSingletonObject() {
        if (singletonObject == null) {
            singletonObject = new CommonModelClass();
        }
        return singletonObject;
    }


    /**
     * used to clear CommonModelClass(SingletonClass) Memory
     */
    public void clear() {
        singletonObject = null;
    }


    public Object clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    //getters and setters starts from here.it is used to set and get a value

    public Activity getMainActivity() {
        return baseActivity;
    }

    public void setMainActivity(Activity baseActivity) {
        this.baseActivity = baseActivity;
    }

}