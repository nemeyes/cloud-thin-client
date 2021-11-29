package ai.sibylla.egp.client;

import android.view.KeyEvent;

public class ClientContext {

    /** 서버 정보 **/
    public static String ServerAddress = "1.214.48.109"; //101.55.120.3
    public static int ServerPortnumber = 5000;
    public static int VideoBufferCapacity = 15;
    public static int VideoBufferSize = 2048 * 1024;
    public static int AudioBufferCapacity = 30;
    public static int AudioBufferSize = 128 * 1024;
    public static boolean AudioEnabled = true;
    public static boolean GyroEnabled = false;
    public static String AppID = "001";
    public static boolean ControllerEnabled = false;

    /** time, timeout 관련 세팅 **/
    public static final int CONNECT_TIMEOUT = 1000;
    public static final int CONTROL_CONTAINER_OPEN = 10000;
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

    public static final short CMD_KEYBOARD_STATE_IND = 5001;
    public static final short CMD_MOUSE_STATE_IND = 5002;
    /*
    public static final short CMD_KEY_DOWN_IND = 2301;		                // Key down
    public static final short CMD_KEY_UP_IND = 2302;		                    // key up
    public static final short CMD_MOUSE_LBD_IND = 2303;		                // touch down
    public static final short CMD_MOUSE_LBU_IND = 2304;		                // touch up
    public static final short CMD_MOUSE_MOVE_IND = 2307;	                    // touch move
    */
    public static final short CMD_GYRO_IND = 9000;			                    // Gyro(Sensor)
    public static final short CMD_GYRO_ROT_IND = 9209;		                // Gyro, Rotate(Sensor)
    public static final short CMD_PINCH_ZOOM_IND = 9001;	                    // Pinch Zoom (Sensor)

    public static final short CMD_ERROR_IND = 7000;

    public static final int DIK_ESCAPE = 0x01;
    public static final int DIK_1 = 0x02;
    public static final int DIK_2 = 0x03;
    public static final int DIK_3 = 0x04;
    public static final int DIK_4 = 0x05;
    public static final int DIK_5 = 0x06;
    public static final int DIK_6 = 0x07;
    public static final int DIK_7 = 0x08;
    public static final int DIK_8 = 0x09;
    public static final int DIK_9 = 0x0A;
    public static final int DIK_0 = 0x0B;
    public static final int DIK_MINUS = 0x0C;    /* - on main keyboard */
    public static final int DIK_EQUALS = 0x0D;
    public static final int DIK_BACK = 0x0E;    /* backspace */
    public static final int DIK_TAB = 0x0F;
    public static final int DIK_Q = 0x10;
    public static final int DIK_W = 0x11;
    public static final int DIK_E = 0x12;
    public static final int DIK_R = 0x13;
    public static final int DIK_T = 0x14;
    public static final int DIK_Y = 0x15;
    public static final int DIK_U = 0x16;
    public static final int DIK_I = 0x17;
    public static final int DIK_O = 0x18;
    public static final int DIK_P = 0x19;
    public static final int DIK_LBRACKET = 0x1A;
    public static final int DIK_RBRACKET = 0x1B;
    public static final int DIK_RETURN = 0x1C;    /* Enter on main keyboard */
    public static final int DIK_LCONTROL = 0x1D;
    public static final int DIK_A = 0x1E;
    public static final int DIK_S = 0x1F;
    public static final int DIK_D = 0x20;
    public static final int DIK_F = 0x21;
    public static final int DIK_G = 0x22;
    public static final int DIK_H = 0x23;
    public static final int DIK_J = 0x24;
    public static final int DIK_K = 0x25;
    public static final int DIK_L = 0x26;
    public static final int DIK_SEMICOLON = 0x27;
    public static final int DIK_APOSTROPHE = 0x28;
    public static final int DIK_GRAVE = 0x29;    /* accent grave */
    public static final int DIK_LSHIFT = 0x2A;
    public static final int DIK_BACKSLASH = 0x2B;
    public static final int DIK_Z = 0x2C;
    public static final int DIK_X = 0x2D;
    public static final int DIK_C = 0x2E;
    public static final int DIK_V = 0x2F;
    public static final int DIK_B = 0x30;
    public static final int DIK_N = 0x31;
    public static final int DIK_M = 0x32;
    public static final int DIK_COMMA = 0x33;
    public static final int DIK_PERIOD = 0x34;    /* . on main keyboard */
    public static final int DIK_SLASH = 0x35;    /* / on main keyboard */
    public static final int DIK_RSHIFT = 0x36;
    public static final int DIK_MULTIPLY = 0x37;    /* * on numeric keypad */
    public static final int DIK_LMENU = 0x38;    /* left Alt */
    public static final int DIK_SPACE = 0x39;
    public static final int DIK_CAPITAL = 0x3A;
    public static final int DIK_F1 = 0x3B;
    public static final int DIK_F2 = 0x3C;
    public static final int DIK_F3 = 0x3D;
    public static final int DIK_F4 = 0x3E;
    public static final int DIK_F5 = 0x3F;
    public static final int DIK_F6 = 0x40;
    public static final int DIK_F7 = 0x41;
    public static final int DIK_F8 = 0x42;
    public static final int DIK_F9 = 0x43;
    public static final int DIK_F10 = 0x44;
    public static final int DIK_NUMLOCK = 0x45;
    public static final int DIK_SCROLL = 0x46;    /* Scroll Lock */
    public static final int DIK_NUMPAD7 = 0x47;
    public static final int DIK_NUMPAD8 = 0x48;
    public static final int DIK_NUMPAD9 = 0x49;
    public static final int DIK_SUBTRACT = 0x4A;    /* - on numeric keypad */
    public static final int DIK_NUMPAD4 = 0x4B;
    public static final int DIK_NUMPAD5 = 0x4C;
    public static final int DIK_NUMPAD6 = 0x4D;
    public static final int DIK_ADD = 0x4E;    /* + on numeric keypad */
    public static final int DIK_NUMPAD1 = 0x4F;
    public static final int DIK_NUMPAD2 = 0x50;
    public static final int DIK_NUMPAD3 = 0x51;
    public static final int DIK_NUMPAD0 = 0x52;
    public static final int DIK_DECIMAL = 0x53;    /* . on numeric keypad */
    public static final int DIK_OEM_102 = 0x56;    /* <> or \| on RT 102-key keyboard (Non-U.S.) */
    public static final int DIK_F11 = 0x57;
    public static final int DIK_F12 = 0x58;
    public static final int DIK_F13 = 0x64;    /*                     (NEC PC98) */
    public static final int DIK_F14 = 0x65;    /*                     (NEC PC98) */
    public static final int DIK_F15 = 0x66;    /*                     (NEC PC98) */
    public static final int DIK_KANA = 0x70;    /* (Japanese keyboard)            */
    public static final int DIK_ABNT_C1 = 0x73;    /* /? on Brazilian keyboard */
    public static final int DIK_CONVERT = 0x79;    /* (Japanese keyboard)            */
    public static final int DIK_NOCONVERT = 0x7B;    /* (Japanese keyboard)            */
    public static final int DIK_YEN = 0x7D;    /* (Japanese keyboard)            */
    public static final int DIK_ABNT_C2 = 0x7E;    /* Numpad . on Brazilian keyboard */
    public static final int DIK_NUMPADEQUALS = 0x8D;    /* = on numeric keypad (NEC PC98) */
    public static final int DIK_PREVTRACK = 0x90;    /* Previous Track (DIK_CIRCUMFLEX on Japanese keyboard) */
    public static final int DIK_AT = 0x91;    /*                     (NEC PC98) */
    public static final int DIK_COLON = 0x92;    /*                     (NEC PC98) */
    public static final int DIK_UNDERLINE = 0x93;    /*                     (NEC PC98) */
    public static final int DIK_KANJI = 0x94;    /* (Japanese keyboard)            */
    public static final int DIK_STOP = 0x95;    /*                     (NEC PC98) */
    public static final int DIK_AX = 0x96;    /*                     (Japan AX) */
    public static final int DIK_UNLABELED = 0x97;    /*                        (J3100) */
    public static final int DIK_NEXTTRACK = 0x99;    /* Next Track */
    public static final int DIK_NUMPADENTER = 0x9C;    /* Enter on numeric keypad */
    public static final int DIK_RCONTROL = 0x9D;
    public static final int DIK_MUTE = 0xA0;    /* Mute */
    public static final int DIK_CALCULATOR = 0xA1;    /* Calculator */
    public static final int DIK_PLAYPAUSE = 0xA2;    /* Play / Pause */
    public static final int DIK_MEDIASTOP = 0xA4;    /* Media Stop */
    public static final int DIK_VOLUMEDOWN = 0xAE;    /* Volume - */
    public static final int DIK_VOLUMEUP = 0xB0;    /* Volume + */
    public static final int DIK_WEBHOME = 0xB2;    /* Web home */
    public static final int DIK_NUMPADCOMMA = 0xB3;    /* , on numeric keypad (NEC PC98) */
    public static final int DIK_DIVIDE = 0xB5;    /* / on numeric keypad */
    public static final int DIK_SYSRQ = 0xB7;
    public static final int DIK_RMENU = 0xB8;    /* right Alt */
    public static final int DIK_PAUSE = 0xC5;    /* Pause */
    public static final int DIK_HOME = 0xC7;    /* Home on arrow keypad */
    public static final int DIK_UP = 0xC8;    /* UpArrow on arrow keypad */
    public static final int DIK_PRIOR = 0xC9;    /* PgUp on arrow keypad */
    public static final int DIK_LEFT = 0xCB;    /* LeftArrow on arrow keypad */
    public static final int DIK_RIGHT = 0xCD;    /* RightArrow on arrow keypad */
    public static final int DIK_END = 0xCF;    /* End on arrow keypad */
    public static final int DIK_DOWN = 0xD0;    /* DownArrow on arrow keypad */
    public static final int DIK_NEXT = 0xD1;    /* PgDn on arrow keypad */
    public static final int DIK_INSERT = 0xD2;    /* Insert on arrow keypad */
    public static final int DIK_DELETE = 0xD3;    /* Delete on arrow keypad */
    public static final int DIK_LWIN = 0xDB;    /* Left Windows key */
    public static final int DIK_RWIN = 0xDC;    /* Right Windows key */
    public static final int DIK_APPS = 0xDD;    /* AppMenu key */
    public static final int DIK_POWER = 0xDE;    /* System Power */
    public static final int DIK_SLEEP = 0xDF;    /* System Sleep */
    public static final int DIK_WAKE = 0xE3;    /* System Wake */
    public static final int DIK_WEBSEARCH = 0xE5;    /* Web Search */
    public static final int DIK_WEBFAVORITES = 0xE6;    /* Web Favorites */
    public static final int DIK_WEBREFRESH = 0xE7;    /* Web Refresh */
    public static final int DIK_WEBSTOP = 0xE8;    /* Web Stop */
    public static final int DIK_WEBFORWARD = 0xE9;    /* Web Forward */
    public static final int DIK_WEBBACK = 0xEA;    /* Web Back */
    public static final int DIK_MYCOMPUTER = 0xEB;    /* My Computer */
    public static final int DIK_MAIL = 0xEC;    /* Mail */
    public static final int DIK_MEDIASELECT = 0xED;    /* Media Select */

    public static final int DIK_BACKSPACE = DIK_BACK;            /* backspace */
    public static final int DIK_NUMPADSTAR = DIK_MULTIPLY;        /* * on numeric keypad */
    public static final int DIK_LALT = DIK_LMENU;           /* left Alt */
    public static final int DIK_CAPSLOCK = DIK_CAPITAL;         /* CapsLock */
    public static final int DIK_NUMPADMINUS = DIK_SUBTRACT;        /* - on numeric keypad */
    public static final int DIK_NUMPADPLUS = DIK_ADD;             /* + on numeric keypad */
    public static final int DIK_NUMPADPERIOD = DIK_DECIMAL;         /* . on numeric keypad */
    public static final int DIK_NUMPADSLASH = DIK_DIVIDE;          /* / on numeric keypad */
    public static final int DIK_RALT = DIK_RMENU;           /* right Alt */
    public static final int DIK_UPARROW = DIK_UP;              /* UpArrow on arrow keypad */
    public static final int DIK_PGUP = DIK_PRIOR;           /* PgUp on arrow keypad */
    public static final int DIK_LEFTARROW = DIK_LEFT;            /* LeftArrow on arrow keypad */
    public static final int DIK_RIGHTARROW = DIK_RIGHT;           /* RightArrow on arrow keypad */
    public static final int DIK_DOWNARROW = DIK_DOWN;            /* DownArrow on arrow keypad */
    public static final int DIK_PGDN = DIK_NEXT;            /* PgDn on arrow keypad */

    public static final int MOUSE_LBUTTON = 0x00;
    public static final int MOUSE_RBUTTON = 0x01;
    public static final int MOUSE_MBUTTON = 0x02;

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
            case CMD_KEYBOARD_STATE_IND : ret = "CMD_KEYBOARD_STATE_IND"; break;
            case CMD_MOUSE_STATE_IND : ret = "CMD_MOUSE_STATE_IND"; break;
            /*
            case CMD_KEY_DOWN_IND: ret = "CMD_KEY_DOWN_IND"; break;
            case CMD_KEY_UP_IND: ret = "CMD_KEY_UP_IND"; break;
            case CMD_MOUSE_LBD_IND: ret = "CMD_MOUSE_LBD_IND"; break;
            case CMD_MOUSE_MOVE_IND: ret = "CMD_MOUSE_MOVE_IND"; break;
            case CMD_MOUSE_LBU_IND: ret = "CMD_MOUSE_LBU_IND"; break;
            case CMD_GYRO_IND: ret = "CMD_GYRO_IND"; break;
            case CMD_GYRO_ROT_IND: ret = "CMD_GYRO_ROT_IND"; break;
            case CMD_PINCH_ZOOM_IND: ret = "CMD_PINCH_ZOOM_IND"; break;
            */
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
