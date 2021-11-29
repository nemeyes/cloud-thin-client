package com.mixedtek.elastics.controller;

import android.view.KeyEvent;

public class ControllerContext {
    public static String ServerAddress = "1.214.48.109"; //101.55.120.3
    public static int ServerPortnumber = 5000;
    public static boolean GyroEnabled = false;

    public static final int CONNECT_TIMEOUT = 5000;
    public static final int CONTROL_CONTAINER_OPEN = 50000;
    public static final int CONTROL_KEEP_ALIVE_TIME = 5000;
    public static final int CONTROL_KEEP_ALIVE_RESPONSE_TIME = 1 * 2000;
    public static int CONTROL_KEEP_CONTROL_GYRO_TIME = 0; 	//유동적으로 변화 시켜놔서 final 을 지움.

    public static final short CMD_NULL = -1;
    public static final short CMD_CREATE_SESSION_REQUEST = 1001; 			// Client 에서 elastics로 create session 요청
    public static final short CMD_CREATE_SESSION_RESPONSE = 1002; 			// elastics에서 Client로 create session에 대한 처리결과 전달
    public static final short CMD_DESTROY_SESSION_INDICATION = 1003;
    public static final short CMD_KEEPALIVE_REQUEST = 1004;	                // Client 에서 Coordinator로 Keepalive Packet 전송
    public static final short CMD_KEEPALIVE_RESPONSE = 1005;	                // Keepalive Pakcket 전송에 대한 응답

    public static final short CMD_CONNECT_CONTROLLER_REQ = 2107;	                // Client 에서 Coordinator 로 Container 생성요청
    public static final short CMD_CONNECT_CONTROLLER_RES = 2108;	                // Container 생성에 대한 응답
    public static final short CMD_DISCONNECT_CONTROLLER_REQ = 2109;			    // Client가 Coordinator 종료 요청.
    public static final short CMD_DISCONNECT_CONTROLLER_RES = 2110;			    // Coordinator가 종료 시, Client에게 LEAVE Notification를 전달한다. Client는 패킷 수신후 socket를 종료한다.

    public static final short CMD_KEY_DOWN_IND = 2301;		                // Key down
    public static final short CMD_KEY_UP_IND = 2302;		                    // key up
    public static final short CMD_MOUSE_LBD_IND = 2303;		                // lb down
    public static final short CMD_MOUSE_LBU_IND = 2304;		                // lb up
    public static final short CMD_MOUSE_RBD_IND = 2305;		                // rb down
    public static final short CMD_MOUSE_RBU_IND = 2306;		                // rb up
    public static final short CMD_MOUSE_MOVE_IND = 2307;	                    // touch move
    public static final short CMD_GYRO_IND = 9000;			                    // Gyro(Sensor)
    public static final short CMD_GYRO_ROT_IND = 9209;		                // Gyro, Rotate(Sensor)
    public static final short CMD_PINCH_ZOOM_IND = 9001;	                    // Pinch Zoom (Sensor)
    public static final short CMD_ERROR_IND = 7000;

    public static String commandToString(short command) {
        String ret = "CMD_NULL";

        switch(command) {
            case CMD_CREATE_SESSION_REQUEST: ret = "CMD_CREATE_SESSION_REQUEST"; break;
            case CMD_CREATE_SESSION_RESPONSE: ret = "CMD_CREATE_SESSION_RESPONSE"; break;
            case CMD_CONNECT_CONTROLLER_REQ: ret = "CMD_CONNECT_CONTROLLER_REQ"; break;
            case CMD_CONNECT_CONTROLLER_RES: ret = "CMD_CONNECT_CONTROLLER_RES"; break;
            case CMD_KEEPALIVE_REQUEST: ret = "CMD_KEEPALIVE_REQUEST"; break;
            case CMD_KEEPALIVE_RESPONSE: ret = "CMD_KEEPALIVE_RESPONSE"; break;
            case CMD_DISCONNECT_CONTROLLER_REQ: ret = "CMD_DISCONNECT_CONTROLLER_REQ"; break;
            case CMD_DISCONNECT_CONTROLLER_RES: ret = "CMD_DISCONNECT_CONTROLLER_RES"; break;
            case CMD_ERROR_IND: ret = "CMD_ERROR_IND"; break;
            case CMD_KEY_DOWN_IND: ret = "CMD_KEY_DOWN_IND"; break;
            case CMD_KEY_UP_IND: ret = "CMD_KEY_UP_IND"; break;
            case CMD_MOUSE_LBD_IND: ret = "CMD_MOUSE_LBD_IND"; break;
            case CMD_MOUSE_MOVE_IND: ret = "CMD_MOUSE_MOVE_IND"; break;
            case CMD_MOUSE_LBU_IND: ret = "CMD_MOUSE_LBU_IND"; break;
            case CMD_GYRO_IND: ret = "CMD_GYRO_IND"; break;
            case CMD_GYRO_ROT_IND: ret = "CMD_GYRO_ROT_IND"; break;
            case CMD_PINCH_ZOOM_IND: ret = "CMD_PINCH_ZOOM_IND"; break;
        }
        return ret;
    }

    public static final short JOYSTICK_START = 105;
    public static final short JOYSTICK_BACK = 104;
    public static final short JOYSTICK_UP = 19;
    public static final short JOYSTICK_DOWN = 20;
    public static final short JOYSTICK_LEFT = 21;
    public static final short JOYSTICK_RIGHT = 22;
    public static final short JOYSTICK_B = 97;
    public static final short JOYSTICK_A = 98;
    public static final short JOYSTICK_Y = 96;
    public static final short JOYSTICK_X = 99;
    public static final short JOYSTICK_RT = 103;
    public static final short JOYSTICK_LT = 102;
    public static final short JOYSTICK_RB = 101;
    public static final short JOYSTICK_LB = 100;

    public static final short REMOTECON_OK = 23;
    public static final short REMOTECON_BACK = 4;
    public static final short REMOTECON_UP = 19;
    public static final short REMOTECON_DOWN = 20;
    public static final short REMOTECON_LEFT = 21;
    public static final short REMOTECON_RIGHT = 22;
    public static final short REMOTECON_RED = 183;
    public static final short REMOTECON_GREEN = 184;
    public static final short REMOTECON_YELLOW = 185;
    public static final short REMOTECON_BLUE = 186;

    public static final short VK_UP = 0x26;
    public static final short VK_DOWN = 0x28;
    public static final short VK_LEFT = 0x25;
    public static final short VK_RIGHT = 0x27;
    public static final short VK_SPACE = 0x20;
    public static final short VK_Z = 0x5A;
    public static final short VK_X = 0x58;
    public static final short VK_D = 0x44;
    public static final short VK_C = 0x43;
    public static final short VK_BACK = 0x08;
    public static final short VK_L_CTRL = 0xA2;
    public static final short VK_R_CTRL = 0xA3;
    public static final short VK_ENTER = 0x0D;
    public static final short VK_L_SHIFT = 0xA0;
    public static final short VK_R_SHIFT = 0xA1;

    private static final int KEY_TABLE[][] = {
            {JOYSTICK_UP, VK_UP},
            {JOYSTICK_DOWN, VK_DOWN},
            {JOYSTICK_LEFT, VK_LEFT},
            {JOYSTICK_RIGHT, VK_RIGHT},
            {JOYSTICK_START, VK_ENTER},
            {JOYSTICK_BACK, VK_BACK},
            {JOYSTICK_B, VK_Z},
            {JOYSTICK_A, VK_X},
            {JOYSTICK_Y, VK_D},
            {JOYSTICK_X, VK_C},
            {JOYSTICK_RT, VK_R_CTRL},
            {JOYSTICK_LT, VK_L_CTRL},
            {JOYSTICK_RB, VK_R_SHIFT},
            {JOYSTICK_LB, VK_L_SHIFT},
            {REMOTECON_UP, VK_UP},
            {REMOTECON_DOWN, VK_DOWN},
            {REMOTECON_LEFT, VK_LEFT},
            {REMOTECON_RIGHT, VK_RIGHT},
            {REMOTECON_OK, VK_ENTER},
            {REMOTECON_RED, VK_Z},
            {REMOTECON_YELLOW, VK_D},
            {REMOTECON_BLUE, VK_C},
            {REMOTECON_GREEN, VK_X}
    };

    public static int convertServerKeyCode(int keycode) {
        int ret = -1;

        for(int i = 0; i < KEY_TABLE.length; i++) {
            if(KEY_TABLE[i][0] == keycode) {
                ret = KEY_TABLE[i][1];
                break;
            }
        }

        return ret;
    }

    public static final int ERROR_CONNECTION_CLOSE = 0;
    public static final int ERROR_CONTAINER_OPEN_FAIL = 1;
    public static final int ERROR_KEEP_ALIVE_FAIL = 2;
    public static final int ERROR_STREAM_REQ_FAIL = 3;
    public static final int ERROR_AUDIO_SAMPLE_RATE = 101;
    public static final int ERROR_MEDIA_BUFFER_OVERFLOW = 201;

    public static String errorCodeToString(int error) {
        switch(error) {
            case ERROR_CONNECTION_CLOSE: return "ERROR_CONNECTION_CLOSE";
            case ERROR_CONTAINER_OPEN_FAIL: return "ERROR_CONTAINER_OPEN_FAIL";
            case ERROR_KEEP_ALIVE_FAIL: return "ERROR_KEEP_ALIVE_FAIL";
            case ERROR_STREAM_REQ_FAIL: return "ERROR_STREAM_REQ_FAIL";
            case ERROR_AUDIO_SAMPLE_RATE: return "ERROR_AUDIO_SAMPLE_RATE";
            case ERROR_MEDIA_BUFFER_OVERFLOW: return "ERROR_MEDIA_BUFFER_OVERFLOW";
        }
        return "ERROR_INVALID";
    }
}
