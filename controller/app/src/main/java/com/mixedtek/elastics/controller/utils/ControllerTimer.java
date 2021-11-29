package com.mixedtek.elastics.controller.utils;

import com.mixedtek.elastics.controller.ControllerContext;

public class ControllerTimer extends TimerManager {
    public static final String CONTROL_KEEP_ALIVE = "CONTROL_KEEP_ALIVE";
    public static final String CONTROL_KEEP_ALIVE_RESPONSE = "CONTROL_KEEP_ALIVE_RESPONSE";
    public static final String CONTROL_GYRO = "CONTROL_GYRO";

    public static void startControlKeepAliveTimer(OnTimeoutListener listener) {
        startTimer(CONTROL_KEEP_ALIVE, ControllerContext.CONTROL_KEEP_ALIVE_TIME, listener);
    }
    public static void stopControlKeepAliveTimer() {
        stopTimer(CONTROL_KEEP_ALIVE);
    }

    public static void startControlKeepAliveResponseTimer(OnTimeoutListener listener) {
        startTimer(CONTROL_KEEP_ALIVE_RESPONSE, ControllerContext.CONTROL_KEEP_ALIVE_RESPONSE_TIME, listener);
    }
    public static void stopControlKeepAliveResponseTimer() {
        stopTimer(CONTROL_KEEP_ALIVE_RESPONSE);
    }

    public static void startControlGyroTimer(OnTimeoutListener listener) {
        startTimer(CONTROL_GYRO, ControllerContext.CONTROL_KEEP_CONTROL_GYRO_TIME, listener);
    }
    public static void stopControlGyroTimer() {
        stopTimer(CONTROL_GYRO);
    }
    public static boolean isControlGyro() {
        return hasTimer(CONTROL_GYRO);
    }
}
