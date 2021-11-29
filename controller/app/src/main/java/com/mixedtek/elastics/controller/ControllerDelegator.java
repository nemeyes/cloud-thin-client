package com.mixedtek.elastics.controller;

import com.mixedtek.elastics.controller.discovery.PlayerDiscovery;
import com.mixedtek.elastics.controller.discovery.PlayerDiscovery.OnDiscoveryEventListener;
import com.mixedtek.elastics.controller.net.PlayerClient;
import com.mixedtek.elastics.controller.net.PlayerClient.OnPlayerClientListener;
import com.mixedtek.elastics.controller.net.ControllerClient;
import com.mixedtek.elastics.controller.net.ControllerClient.OnControllerListener;

import android.util.Log;

public class ControllerDelegator {
    private static final String TAG = "ControllerDelegator";

    public static final int STATE_DISCONNECT = 0;
    public static final int STATE_PLAYER_DISCOVERYING = 1;
    public static final int STATE_PLAYER_DISCOVERED = 2;
    public static final int STATE_PLAYER_CLIENT_CONNECTING = 3;
    public static final int STATE_PLAYER_CLIENT_CONNECTED = 4;
    public static final int STATE_CONTROLLER_CLIENT_CONNECTING = 5;
    public static final int STATE_CONTROLLER_CLIENT_CONNECTED = 6;

    private MainActivity mFront;

    private int mState = STATE_DISCONNECT;
    private PlayerDiscovery mPlayerDiscovery;
    private PlayerClient mPlayerClient;
    private ControllerClient mControllerClient;

    private OnStateChangeListener mStateChangeListener;
    public interface OnStateChangeListener {
        public void onStateChanged(int state);
    }

    ControllerDelegator(MainActivity front) {
        mFront = front;
        setState(STATE_DISCONNECT);
    }

    public void setStateChangeListener(OnStateChangeListener listener) {
        mStateChangeListener = listener;
    }

    private void setState(int state) {
        mState = state;
        if(mStateChangeListener != null) {
            mStateChangeListener.onStateChanged(state);
        }
    }

    public int getState() {
        return mState;
    }

    public void start() {
        if(mState != STATE_DISCONNECT) {
            Log.w(TAG, "Client already started");
            return;
        }
        setState(STATE_PLAYER_DISCOVERYING);
        if(mPlayerDiscovery!=null) {
            mPlayerDiscovery.stopDiscovery();
            mPlayerDiscovery.release();
            mPlayerDiscovery = null;
        }
        mPlayerDiscovery = new PlayerDiscovery();
        mPlayerDiscovery.setDiscoveryEventListener(mDiscoveryEventListener);
        mPlayerDiscovery.initialize(mFront);
        mPlayerDiscovery.startDiscovery();
    }

    public void stop() {
        if(mState == STATE_DISCONNECT) {
            Log.i(TAG, "Client already stopped");
            return;
        }
        setState(STATE_DISCONNECT);
        stopClient();
        if(mPlayerDiscovery!=null) {
            mPlayerDiscovery.stopDiscovery();
            mPlayerDiscovery.release();
            mPlayerDiscovery = null;
        }
    }

    public boolean sendKeyDownEvent(int keyCode) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestKeyDown(keyCode);
    }

    public boolean sendKeyUpEvent(int keyCode) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestKeyUp(keyCode);
    }

    public boolean sendLMouseDownEvent(int x, int y) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestLMouseDown(x, y);
    }
    public boolean sendLMouseUpEvent(int x, int y) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestLMouseUp(x, y);
    }

    public boolean sendRMouseDownEvent(int x, int y) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestRMouseDown(x, y);
    }
    public boolean sendRMouseUpEvent(int x, int y) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestRMouseUp(x, y);
    }

    public boolean sendMouseMoveEvent(int x, int y) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestMouseMove(x, y);
    }

    public boolean sendGyroRotEvent(float x, float y, float z, float w) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestGyroRot(x, y, z, w);
    }

    public boolean sendPinchZoomEvent(int delta) {
        if(mState != STATE_CONTROLLER_CLIENT_CONNECTED)
            return false;

        return mControllerClient.requestPinchZoom(delta);
    }

    private void connectPlayerClient(final String host, final int port) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                setState(STATE_PLAYER_CLIENT_CONNECTING);
                boolean connected;
                if(mPlayerClient == null) {
                    mPlayerClient = new PlayerClient();
                    mPlayerClient.setOnControllerListener(mPlayerClientListener);
                }
                connected = mPlayerClient.startClient(host, port);
                if(connected) {
                    setState(STATE_PLAYER_CLIENT_CONNECTED);
                } else {
                    Log.e(TAG, "connect error");
                    stop();
                }
            }
        });
        thread.start();
    }

    private void disconnectPlayerClient() {
        if(mPlayerClient!=null) {
            //if(mPlayerClient.isRun())
            mPlayerClient.stopClient();
            mPlayerClient= null;
        }
    }

    private void connectControllerClient(final String ip, final int port, final String uuid) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean connected;
                if(mControllerClient == null) {
                    mControllerClient = new ControllerClient();
                    mControllerClient.setOnControlListener(mControllerListener);
                }
                connected = mControllerClient.startClient(ip, port, uuid, 1024 * 4, 1024 * 4, ControllerContext.CONNECT_TIMEOUT);
                if(connected) {
                    mControllerClient.requestCreateSession();
                } else {
                    Log.e(TAG, "connect error");
                    stop();
                }
            }
        });
        thread.start();
    }

    private void disconnectControllerClient() {
        if(mControllerClient != null) {
            if(mControllerClient.isRun()) {
                mControllerClient.requestDisconnectController();
            }
            mControllerClient.stopClient();
            mControllerClient = null;
        }
    }

    private void stopClient() {
        disconnectControllerClient();
        disconnectPlayerClient();
    }

    public OnDiscoveryEventListener mDiscoveryEventListener = new OnDiscoveryEventListener() {
        @Override
        public void onServiceResolved(String address, int port) {
            setState(STATE_PLAYER_DISCOVERED);
            connectPlayerClient(address, port);
        }

        @Override
        public void onServiceResolveFailed() {
            /*
            if(getState()==STATE_PLAYER_CLIENT_CONNECTED) {
                disconnectPlayerClient();
            }
            if(getState()==STATE_CONTROLLER_CLIENT_CONNECTED) {
                disconnectControllerClient();
                disconnectPlayerClient();
            }
            */
            setState(STATE_DISCONNECT);
        }

        @Override
        public void onServerLost() {
            if(getState()==STATE_PLAYER_CLIENT_CONNECTED) {
                disconnectPlayerClient();
            }
            if(getState()==STATE_CONTROLLER_CLIENT_CONNECTED) {
                disconnectControllerClient();
                disconnectPlayerClient();
            }
            setState(STATE_DISCONNECT);
        }
    };

    public OnPlayerClientListener mPlayerClientListener = new OnPlayerClientListener() {
        @Override
        public void onConnected(String address, int portnumber, String uuid) {
            setState(STATE_CONTROLLER_CLIENT_CONNECTING);
            connectControllerClient(address, portnumber, uuid);
        }

        @Override
        public void onDisconnected(String address, int portnumber, String uuid) {
            if(getState()==STATE_CONTROLLER_CLIENT_CONNECTED) {
                disconnectControllerClient();
                setState(STATE_PLAYER_CLIENT_CONNECTED);
            }
        }
    };

    public OnControllerListener mControllerListener = new OnControllerListener() {
        @Override
        public void onConnectedToContainer() {
            setState(STATE_CONTROLLER_CLIENT_CONNECTED);
        }

        @Override
        public void onDisconnectedFromContainer() {
            disconnectControllerClient();
        }

        @Override
        public void onError(int error) {
            disconnectControllerClient();
        }
    };

    public static String stateToString(int state) {
        switch(state) {
            case STATE_DISCONNECT:
                return "STATE_DISCONNECT";
            case STATE_PLAYER_DISCOVERYING:
                return "STATE_PLAYER_DISCOVERYING";
            case STATE_PLAYER_DISCOVERED:
                return "STATE_PLAYER_DISCOVERED";
            case STATE_CONTROLLER_CLIENT_CONNECTING:
                return "STATE_CONTROLLER_CLIENT_CONNECTING";
            case STATE_CONTROLLER_CLIENT_CONNECTED:
                return "STATE_CONTROLLER_CLIENT_CONNECTED";
        }
        return "STATE_NONE";
    }
}
