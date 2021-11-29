package com.mixedtek.elastics.player;

import com.mixedtek.elastics.player.utils.Preferences;

import android.app.Application;
import android.content.Context;

public class PlayerApplication extends Application  {
    private static Context mApplicationContext = null;

    @Override
    public void onCreate() {
        super.onCreate();

        Preferences.init(this);

        mApplicationContext = this;
    }

    public static Context getAppContext() {
        return mApplicationContext;
    }
}
