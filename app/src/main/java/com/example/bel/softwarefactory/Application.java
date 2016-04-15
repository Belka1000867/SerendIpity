package com.example.bel.softwarefactory;

import com.facebook.FacebookSdk;

import org.androidannotations.annotations.EApplication;

@EApplication
public class Application extends android.app.Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FacebookSdk.sdkInitialize(getApplicationContext());
    }
}
