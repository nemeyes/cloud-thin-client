package ai.sibylla.egp.client.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

    private static final String TAG = "Preferences";
    private static final String NAME = "ai.sibylla.egp.client";

    private static Context mApplicationContext = null;

    private static final String KEY_SERVER_ADDRESS = "server_address";
    private static final String KEY_SERVER_PORT = "server_port";
    private static final String KEY_VIDEO_BUFFER_CAPACITY = "video_buffer_capacity";
    private static final String KEY_VIDEO_BUFFER_SIZE = "video_buffer_size";
    private static final String KEY_ENABLE_AUDIO = "enable_audio";
    private static final String KEY_AUDIO_BUFFER_CAPACITY = "audio_buffer_capacity";
    private static final String KEY_AUDIO_BUFFER_SIZE = "audio_buffer_size";
    private static final String KEY_ENABLE_GYRO = "enable_gyro";
    private static final String KEY_APP_ID = "key_app_id";
    private static final String KEY_ENABLE_CONTROLLER = "enable_controller";

    public static void init(Context context) {
        mApplicationContext = context;
    }

    private static SharedPreferences getPreferences() {
        if (mApplicationContext == null) {
            return null;
        }
        return mApplicationContext.getSharedPreferences(NAME, Activity.MODE_PRIVATE);
    }

    private static void putString(String key, String value) {
        SharedPreferences pref = getPreferences();
        if (pref == null) {
            return;
        }
        Editor editor = pref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    private static void putInt(String key, int value) {
        SharedPreferences pref = getPreferences();
        if (pref == null) {
            return;
        }
        Editor editor = pref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    private static void putBoolean(String key, boolean value) {
        SharedPreferences pref = getPreferences();
        if (pref == null) {
            return;
        }
        Editor editor = pref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    private static String getString(String key, String def) {
        SharedPreferences pref = getPreferences();
        if (pref == null) {
            return "";
        }
        return pref.getString(key, def);
    }

    private static int getInt(String key, int def) {
        SharedPreferences pref = getPreferences();
        if (pref == null) {
            return -1;
        }
        return pref.getInt(key, def);
    }

    private static boolean getBoolean(String key, boolean def) {
        SharedPreferences pref = getPreferences();
        if (pref == null) {
            return false;
        }
        return pref.getBoolean(key, def);
    }

    public static void setServerAddress(String address) {
        putString(KEY_SERVER_ADDRESS, address);
    }
    public static void setServerPort(String port) {
        putString(KEY_SERVER_PORT, port);
    }
    public static void setVideoBufferCapacity(int capacity) {
        putInt(KEY_VIDEO_BUFFER_CAPACITY, capacity);
    }
    public static void setVideoBufferSize(int size) {
        putInt(KEY_VIDEO_BUFFER_SIZE, size);
    }
    public static void setAudioEnable(boolean value) {
        putBoolean(KEY_ENABLE_AUDIO, value);
    }
    public static void setAudioBufferCapacity(int capacity) {
        putInt(KEY_AUDIO_BUFFER_CAPACITY, capacity);
    }
    public static void setAudioBufferSize(int size) {
        putInt(KEY_AUDIO_BUFFER_SIZE, size);
    }
    public static void setGyroEnable(boolean value) {
        putBoolean(KEY_ENABLE_GYRO, value);
    }
    public static void setAppID(String id) {
        putString(KEY_APP_ID, id);
    }
    public static void setControllerEnable(boolean value) {
        putBoolean(KEY_ENABLE_CONTROLLER, value);
    }

    public static String getServerAddress() {
        return getString(KEY_SERVER_ADDRESS, "192.168.0.1");
    }
    public static String getServerPort() {
        return getString(KEY_SERVER_PORT, "5000");
    }
    public static int getVideoBufferCapacity() {
        return getInt(KEY_VIDEO_BUFFER_CAPACITY, 15);
    }
    public static int getVideoBufferSize() {
        return getInt(KEY_VIDEO_BUFFER_SIZE, 2048);
    }
    public static boolean getAudioEnable() {
        return getBoolean(KEY_ENABLE_AUDIO, true);
    }
    public static int getAudioBufferCapacity() {
        return getInt(KEY_AUDIO_BUFFER_CAPACITY, 30);
    }
    public static int getAudioBufferSize() {
        return getInt(KEY_AUDIO_BUFFER_SIZE, 128);
    }
    public static boolean getGyroEnable() {
        return getBoolean(KEY_ENABLE_GYRO, false);
    }
    public static String getAppID() {
        return getString(KEY_APP_ID, "001");
    }
    public static boolean getControllerEnable() {
        return getBoolean(KEY_ENABLE_CONTROLLER, false);
    }
}
