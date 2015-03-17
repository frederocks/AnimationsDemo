package com.example.android.animationsdemo;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Jim
 * Date: 3/4/13
 * Time: 3:01 PM
 * To change this template use File | Settings | File Templates.
 */
public class LogHelper {
    public static void LogBundleKeys(String logTag, String keyPrefix, Bundle bundle) {
        boolean loggedAtLeastOneKey = false;
        if(bundle != null) {
            Set<String> keys = bundle.keySet();
            if (keys != null) {
                for(String key:keys) {
                    Log.d(logTag, keyPrefix + key);
                    loggedAtLeastOneKey = true;
                }
            }
        }

        if (!loggedAtLeastOneKey)
            Log.d(logTag, keyPrefix + " Bundle is null or contains no keys");

    }

    public static void logInfo(String logTag, Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();

        Log.d(logTag, String.format("Bitmap info - Height:%d | Width:%d", height, width));
    }
}
