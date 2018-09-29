package com.infinity.dev.vankin;

import android.app.Application;

import com.google.android.gms.ads.MobileAds;

public class VankinApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        MobileAds.initialize(this, getString(R.string.admob_app_id));
    }
}