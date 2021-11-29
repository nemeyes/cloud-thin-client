package com.mixedtek.elastics.player.utils;

import com.mixedtek.elastics.player.PlayerContext;

public class PlayerTimer extends TimerManager {

    // TimerManager를 상속받은 다른 클래스에서 TimerName을 동일하게 가져가면 동일한 타이머를 사용하므로 문제가 발생함.
    //prefix를 붙이든 이름을 다르게 해야함. 추후 enum고려
    public static final String CONTROL_CONTAINER_OPEN = "CONTROL_CONTAINER_OPEN"; // slot open(스트리밍 준비) timeout 체크
    public static final String CONTROL_KEEP_ALIVE = "CONTROL_KEEP_ALIVE"; // control client keep alive timer
    public static final String CONTROL_KEEP_ALIVE_RESPONSE = "CONTROL_KEEP_ALIVE_RESPONSE"; //  keep alive response timer
    public static final String CONTROL_GYRO = "CONTROL_GYRO";

    // CONTROL_NOTI_WAIT
    public static void startControlContainerOpenTimer(OnTimeoutListener listener) {
        startTimer(CONTROL_CONTAINER_OPEN, PlayerContext.CONTROL_CONTAINER_OPEN, listener);
    }

    public static void stopControlContainerOpenTimer() {
        stopTimer(CONTROL_CONTAINER_OPEN);
    }

    // CONTROL_KEEP_ALIVE
    public static void startControlKeepAliveTimer(OnTimeoutListener listener) {
        startTimer(CONTROL_KEEP_ALIVE, PlayerContext.CONTROL_KEEP_ALIVE_TIME, listener);
    }

    public static void stopControlKeepAliveTimer() {
        stopTimer(CONTROL_KEEP_ALIVE);
    }

    // CONTROL_KEEP_ALIVE_RESPONSE
    public static void startControlKeepAliveResponseTimer(OnTimeoutListener listener) {
        startTimer(CONTROL_KEEP_ALIVE_RESPONSE, PlayerContext.CONTROL_KEEP_ALIVE_RESPONSE_TIME, listener);
    }

    public static void stopControlKeepAliveResponseTimer() {
        stopTimer(CONTROL_KEEP_ALIVE_RESPONSE);
    }

    // CONTROL_GYRO
    public static void startControlGyroTimer(OnTimeoutListener listener) {
        startTimer(CONTROL_GYRO, PlayerContext.CONTROL_KEEP_CONTROL_GYRO_TIME, listener);
    }

    public static void stopControlGyroTimer() {
        stopTimer(CONTROL_GYRO);
    }

    public static boolean isControlGyro() {
        return hasTimer(CONTROL_GYRO);
    }
}
