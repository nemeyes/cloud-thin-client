package com.mixedtek.elastics.controller.utils;

import java.util.concurrent.ConcurrentHashMap;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class TimerManager {

    private static final String TAG = "TimerManager";
    public interface OnTimeoutListener {
        public void onTimeout(String type);
    };

    private static TimerManager mManager;
    private ConcurrentHashMap<String, OnTimeoutListener> mTimerMap = new ConcurrentHashMap<String, OnTimeoutListener>();

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            String type = (String)msg.obj;
            Log.d(TAG, "onTimeout " + type);

            OnTimeoutListener listener = mTimerMap.get(type);
            if(listener != null) {
                listener.onTimeout(type);
                mTimerMap.remove(type);
            }

            super.handleMessage(msg);
        }
    };

    public static synchronized TimerManager getInstance() {
        if(mManager == null) {
            mManager = new TimerManager();
        }

        return mManager;
    }

    public static Handler getHandler() {
        return getInstance().mHandler;
    }

    public static ConcurrentHashMap<String, OnTimeoutListener> getTimerMap() {
        return getInstance().mTimerMap;
    }

    public static boolean hasTimer(String type) {
        return getTimerMap().get(type) != null;
    }

    protected static void startTimer(String type, long delayTime, OnTimeoutListener listener) {
        Log.d(TAG, "startTimer " + type + "(" + delayTime + ")");

        stopTimer(type);

        getHandler().sendMessageDelayed(getHandler().obtainMessage(type.hashCode(), type), delayTime);

        getTimerMap().put(type, listener);
    }


    protected static void stopTimer(String type) {
        getHandler().removeMessages(type.hashCode());

        if(getTimerMap().remove(type) == null) {
            Log.d(TAG, "[" + type + "]" + "timer is not found !!!");
        } else {
            Log.d(TAG, "[" + type + "]" + "stopTimer ");
        }
    }
}
