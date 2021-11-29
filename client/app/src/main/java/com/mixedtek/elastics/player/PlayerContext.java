package com.mixedtek.elastics.player;

import android.view.KeyEvent;

public class PlayerContext {

    /** 서버 정보 **/
    public static String ServerAddress = "1.214.48.109"; //101.55.120.3
    public static int ServerPortnumber = 5000;
    public static int VideoBufferCapacity = 15;
    public static int VideoBufferSize = 2048 * 1024;
    public static int AudioBufferCapacity = 30;
    public static int AudioBufferSize = 128 * 1024;
    public static boolean AudioEnabled = true;
    public static boolean GyroEnabled = false;
    public static boolean OnDemandConnection = false;
    public static String AppID = "001";
    public static boolean ControllerEnabled = false;

    /** time, timeout 관련 세팅 **/
    public static final int CONNECT_TIMEOUT = 5000;
    public static final int CONTROL_CONTAINER_OPEN = 50000;
    public static final int CONTROL_KEEP_ALIVE_TIME = 5000;
    public static final int CONTROL_KEEP_ALIVE_RESPONSE_TIME = 1 * 2000;
    public static int CONTROL_KEEP_CONTROL_GYRO_TIME = 0; 	//유동적으로 변화 시켜놔서 final 을 지움.

    // Command client Client <-> Coordinator
    public static final short CMD_NULL = -1;
    public static final short CMD_CREATE_SESSION_REQUEST = 1001; 			// Client 에서 elastics로 create session 요청
    public static final short CMD_CREATE_SESSION_RESPONSE = 1002; 			// elastics에서 Client로 create session에 대한 처리결과 전달
    public static final short CMD_DESTROY_SESSION_INDICATION = 1003;
    public static final short CMD_KEEPALIVE_REQUEST = 1004;	                // Client 에서 Coordinator로 Keepalive Packet 전송
    public static final short CMD_KEEPALIVE_RESPONSE = 1005;	                // Keepalive Pakcket 전송에 대한 응답

    public static final short CMD_CONNECT_CLIENT_REQ = 2101;	                // Client 에서 Coordinator 로 Container 생성요청
    public static final short CMD_CONNECT_CLIENT_RES = 2102;	                // Container 생성에 대한 응답
    public static final short CMD_CONTAINER_INFO_IND = 2104;		            // Coordinator에서 Client로 Container 정보를 Noti 한다.
    public static final short CMD_DISCONNECT_CLIENT_REQ = 2105;			    // Client가 Coordinator 종료 요청.
    public static final short CMD_DISCONNECT_CLIENT_RES = 2106;			    // Coordinator가 종료 시, Client에게 LEAVE Notification를 전달한다. Client는 패킷 수신후 socket를 종료한다.

    // Streaming Client  Client <-> Container
    public static final short CMD_IFRAME_REQ_IND = 2201;		                // Client 에서 Container으로 Iframe 전송 요청
    public static final short CMD_END2END_DATA_IND = 2202;		            // Client 에서 Container으로 XML 정보를 전달한다

    public static final short CMD_PLAY_REQ = 4001;			                    // Client 에서 수신 할 Stream type 을 요청
    public static final short CMD_PLAY_RES = 4002;			                    // CMD_PLAY_REQ 응답 전송

    public static final short CMD_KEY_DOWN_IND = 2301;		                // Key down
    public static final short CMD_KEY_UP_IND = 2302;		                    // key up
    public static final short CMD_MOUSE_LBD_IND = 2303;		                // touch down
    public static final short CMD_MOUSE_LBU_IND = 2304;		                // touch up
    public static final short CMD_MOUSE_MOVE_IND = 2307;	                    // touch move
    public static final short CMD_GYRO_IND = 9000;			                    // Gyro(Sensor)
    public static final short CMD_GYRO_ROT_IND = 9209;		                // Gyro, Rotate(Sensor)
    public static final short CMD_PINCH_ZOOM_IND = 9001;	                    // Pinch Zoom (Sensor)

    public static final short CMD_ERROR_IND = 7000;

    public static String commandToString(short command) {
        String ret = "CMD_NULL";

        switch(command) {
            // Command client
            case CMD_CREATE_SESSION_REQUEST: ret = "CMD_CREATE_SESSION_REQUEST"; break;
            case CMD_CREATE_SESSION_RESPONSE: ret = "CMD_CREATE_SESSION_RESPONSE"; break;
            case CMD_CONNECT_CLIENT_REQ: ret = "CMD_CONNECT_CLIENT_REQ"; break;
            case CMD_CONNECT_CLIENT_RES: ret = "CMD_CONNECT_CLIENT_RES"; break;
            case CMD_CONTAINER_INFO_IND: ret = "CMD_CONTAINER_INFO_IND"; break;
            case CMD_KEEPALIVE_REQUEST: ret = "CMD_KEEPALIVE_REQUEST"; break;
            case CMD_KEEPALIVE_RESPONSE: ret = "CMD_KEEPALIVE_RESPONSE"; break;
            case CMD_DISCONNECT_CLIENT_RES: ret = "CMD_DISCONNECT_CLIENT_RES"; break;

            // Streaming Client
            case CMD_IFRAME_REQ_IND: ret = "CMD_IFRAME_REQ_IND"; break;
            case CMD_END2END_DATA_IND: ret = "CMD_END2END_DATA_IND"; break;
            case CMD_ERROR_IND: ret = "CMD_ERROR_IND"; break;
            case CMD_PLAY_REQ: ret = "CMD_PLAY_REQ"; break;
            case CMD_PLAY_RES: ret = "CMD_PLAY_RES"; break;
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

    /**
     * key code
     * window key code를 따름
     * https://msdn.microsoft.com/en-us/library/windows/desktop/dd375731(v=vs.85).aspx
     */

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

    /*
    private static final int KEY_TABLE[][] = {
            {KeyEvent.KEYCODE_DPAD_UP, 0x26},
            {KeyEvent.KEYCODE_DPAD_DOWN, 0x28},
            {KeyEvent.KEYCODE_DPAD_LEFT, 0x25},
            {KeyEvent.KEYCODE_DPAD_RIGHT, 0x27},
            {KeyEvent.KEYCODE_DPAD_CENTER, 0x0D},
            {KeyEvent.KEYCODE_NUMPAD_0, 0x30},
            {KeyEvent.KEYCODE_NUMPAD_1, 0x31},
            {KeyEvent.KEYCODE_NUMPAD_2, 0x32},
            {KeyEvent.KEYCODE_NUMPAD_3, 0x33},
            {KeyEvent.KEYCODE_NUMPAD_4, 0x34},
            {KeyEvent.KEYCODE_NUMPAD_5, 0x35},
            {KeyEvent.KEYCODE_NUMPAD_6, 0x36},
            {KeyEvent.KEYCODE_NUMPAD_7, 0x37},
            {KeyEvent.KEYCODE_NUMPAD_8, 0x38},
            {KeyEvent.KEYCODE_NUMPAD_9, 0x39},
            {KeyEvent.KEYCODE_F6, 0x75},
            {KeyEvent.KEYCODE_F7, 0x76},
    };
    */

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

    /**
     * error code 많아지면 클래스로 분리
     */
    // Client error
    public static final int ERROR_CONNECTION_CLOSE = 0;
    public static final int ERROR_CONTAINER_OPEN_FAIL = 1;
    public static final int ERROR_KEEP_ALIVE_FAIL = 2;
    public static final int ERROR_STREAM_REQ_FAIL = 3;

    // VideoDecoder error
    public static final int ERROR_AUDIO_SAMPLE_RATE = 101;

    // Streaming Client error
    public static final int ERROR_MEDIA_BUFFER_OVERFLOW = 201;

    public static String errorCodeToString(int error) {
        switch(error) {
            // Client error
            case ERROR_CONNECTION_CLOSE: return "ERROR_CONNECTION_CLOSE";
            case ERROR_CONTAINER_OPEN_FAIL: return "ERROR_CONTAINER_OPEN_FAIL";
            case ERROR_KEEP_ALIVE_FAIL: return "ERROR_KEEP_ALIVE_FAIL";
            case ERROR_STREAM_REQ_FAIL: return "ERROR_STREAM_REQ_FAIL";

            // VideoDecoder error
            case ERROR_AUDIO_SAMPLE_RATE: return "ERROR_AUDIO_SAMPLE_RATE";

            // Streaming Client error
            case ERROR_MEDIA_BUFFER_OVERFLOW: return "ERROR_MEDIA_BUFFER_OVERFLOW";
        }

        return "ERROR_INVALID";
    }
}
