package com.example.android.animationsdemo;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.parse.Parse;
import com.parse.ParseObject;

/**
 * Created by lfredericks on 3/21/2015.
 */
public class testActivity extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        doBindService();
        ParseObject testObject = new ParseObject("TestObject");
        testObject.put("foo", "bar");
        testObject.saveInBackground();

    }

    void doBindService() {

        // Establish a connection with the service. We use an explicit
        // class name because we want a specific service implementation that
        // we know will be running in our own process (and thus won't be
        // supporting component replacement by other applications).

        Parse.enableLocalDatastore(this);

        Parse.initialize(this, "zeyZ4fj39owhcwh4d4mBYRZ7lA639HxzCmDyw9au", "kEEtXYv9SzCVu3lXWUVQclVRURu8h4avBovsE0rb");
    }

}