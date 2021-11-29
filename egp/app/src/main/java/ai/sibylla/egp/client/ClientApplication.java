package ai.sibylla.egp.client;

import ai.sibylla.egp.client.utils.Preferences;

import android.app.Application;
import android.content.Context;

public class ClientApplication extends Application  {
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
