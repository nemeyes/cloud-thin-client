package com.mixedtek.elastics.controller.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.mixedtek.elastics.controller.ControllerContext;
import com.mixedtek.elastics.controller.net.base.LightweightClient;
import com.mixedtek.elastics.controller.net.data.Header;
import com.mixedtek.elastics.controller.utils.ByteUtils;
import com.mixedtek.elastics.controller.utils.ControllerTimer;
import com.mixedtek.elastics.controller.utils.TimerManager.OnTimeoutListener;

public class ControllerClient extends LightweightClient {
    private static final String TAG = "ControllerClient";
    private OnControllerListener mControllerListener;

    private String mContainerUUID = null;

    public interface OnControllerListener {
        public void onConnectedToContainer();
        public void onDisconnectedFromContainer();
        public void onError(int error);
    };

    public void setOnControlListener(OnControllerListener listener) {
        mControllerListener = listener;
    }

    public boolean startClient(final String ip, final int port, final String uuid, final int sndBufferSize, final int rcvBufferSize, final int timeout) {
        setDestUUID(uuid);
        mContainerUUID = uuid;
        return super.startClient(ip, port, sndBufferSize, rcvBufferSize, timeout);
    }

    @Override
    public boolean stopClient() {
        ControllerTimer.stopControlKeepAliveTimer();
        ControllerTimer.stopControlKeepAliveResponseTimer();
        ControllerTimer.stopControlGyroTimer();

        return super.stopClient();
    }

    @Override
    public void startReaderThread() {
        mIsRun = true;

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mIsRun) {
                    Header header = readHeader();
                    JSONObject body = null;
                    short command;
                    int bodySize;
                    int rcode = -1;
                    if(header == null) {
                        continue;
                    }

                    command = header.getCommand();
                    bodySize = header.getLength();

                    if(readBody(bodySize) < 0) {
                        mControllerListener.onError(ControllerContext.ERROR_CONNECTION_CLOSE);
                        continue;
                    }

                    switch(command) {
                        case ControllerContext.CMD_CREATE_SESSION_RESPONSE:
                            rcode = getCreateSessionResponseCode(mBody);
                            if(rcode == 0) {
                                byteToSrcUUID(mBody, 4);
                                requestConnectController();
                            } else {
                                if(mControllerListener!=null)
                                    mControllerListener.onDisconnectedFromContainer();
                            }
                            break;

                        case ControllerContext.CMD_CONNECT_CONTROLLER_RES:
                            try {
                                body = new JSONObject(new String(mBody));
                                rcode = body.getInt("rcode");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(rcode == 0) {
                                if(mControllerListener!=null)
                                    mControllerListener.onConnectedToContainer();
                                ControllerTimer.startControlKeepAliveTimer(mTimeoutListener);
                            } else {
                                if(mControllerListener!=null)
                                    mControllerListener.onDisconnectedFromContainer();
                            }
                            break;

                        case ControllerContext.CMD_KEEPALIVE_RESPONSE:
                            ControllerTimer.startControlKeepAliveTimer(mTimeoutListener);
                            ControllerTimer.stopControlKeepAliveResponseTimer();
                            break;

                        case ControllerContext.CMD_DISCONNECT_CONTROLLER_RES:
                            if(mControllerListener!=null)
                                mControllerListener.onDisconnectedFromContainer();
                            ControllerTimer.stopControlKeepAliveResponseTimer();
                            ControllerTimer.stopControlKeepAliveResponseTimer();
                            break;

                        case ControllerContext.CMD_DESTROY_SESSION_INDICATION:
                            if(mControllerListener!=null)
                                mControllerListener.onDisconnectedFromContainer();
                            break;
                    };
                }
            }
        });
        thread.start();
    }

    private OnTimeoutListener mTimeoutListener = new OnTimeoutListener() {
        public void onTimeout(String type) {
            if(type.equals(ControllerTimer.CONTROL_KEEP_ALIVE)) {
                requestKeepalive();
            } else if(type.equals(ControllerTimer.CONTROL_KEEP_ALIVE_RESPONSE)) {
                keepAliveResponseTimeout();
            }
        }
    };

    private void keepAliveResponseTimeout() {
        ControllerTimer.stopControlKeepAliveTimer();
        mControllerListener.onError(ControllerContext.ERROR_KEEP_ALIVE_FAIL);
    }

    public void requestCreateSession() {
        Header header = Header.newInstance(ControllerContext.CMD_CREATE_SESSION_REQUEST);
        header.setInitialSrcUUID();
        header.setInitialDestUUID();

        sendRequest(header);
    }

    public void requestConnectController() {
        Header header = Header.newInstance(ControllerContext.CMD_CONNECT_CONTROLLER_REQ);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID();

        JSONObject json = new JSONObject();
        try {
            json.put("uuid", mContainerUUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendRequest(header, json.toString());
    }

    public void requestDisconnectController() {
        Header header = Header.newInstance(ControllerContext.CMD_DISCONNECT_CONTROLLER_REQ);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID();

        sendRequest(header);
    }

    public void requestKeepalive() {
        ControllerTimer.startControlKeepAliveResponseTimer(mTimeoutListener);
        Header header = Header.newInstance(ControllerContext.CMD_KEEPALIVE_REQUEST);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID();
        sendRequest(header);
    }

    public boolean requestKeyDown(int keyCode) {
        Header header = Header.newInstance(ControllerContext.CMD_KEY_DOWN_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(keyCode, buff, 4);

        sendRequest(header, buff);
        return true;
    }

    public boolean requestKeyUp(int keyCode) {
        Header header = Header.newInstance(ControllerContext.CMD_KEY_UP_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(keyCode, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestLMouseDown(int x, int y) {
        Header header = Header.newInstance(ControllerContext.CMD_MOUSE_LBD_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestLMouseUp(int x, int y) {
        Header header = Header.newInstance(ControllerContext.CMD_MOUSE_LBU_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestRMouseDown(int x, int y) {
        Header header = Header.newInstance(ControllerContext.CMD_MOUSE_RBD_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestRMouseUp(int x, int y) {
        Header header = Header.newInstance(ControllerContext.CMD_MOUSE_RBU_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseDown(int x, int y) {
        Header header = Header.newInstance(ControllerContext.CMD_MOUSE_LBD_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseMove(int x, int y) {
        Header header = Header.newInstance(ControllerContext.CMD_MOUSE_MOVE_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseUp(int x, int y) {
        Header header = Header.newInstance(ControllerContext.CMD_MOUSE_LBU_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestGyro(float x, float y, float z) {
        Header header = Header.newInstance(ControllerContext.CMD_GYRO_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[12];
        ByteUtils.floatToByte(x, buff, 0);
        ByteUtils.floatToByte(y, buff, 4);
        ByteUtils.floatToByte(z, buff, 8);
        sendRequest(header, buff);
        return true;
    }

    public void Gryo(float x, float y, float z, float w) {
        Header header = Header.newInstance(ControllerContext.CMD_GYRO_ROT_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);
        byte buff[] = new byte[16];

        ByteUtils.floatToByte(x, buff, 0);
        ByteUtils.floatToByte(y, buff, 4);
        ByteUtils.floatToByte(z, buff, 8);
        ByteUtils.floatToByte(w, buff, 12);
        sendRequest(header, buff);
    }

    public boolean requestGyroRot(float x, float y, float z, float w) {
        if(ControllerContext.CONTROL_KEEP_CONTROL_GYRO_TIME != 0) {
            if(!ControllerTimer.isControlGyro()) {
                Gryo(x,y,z,w);
                ControllerTimer.startControlGyroTimer(mTimeoutListener);
                return true;
            }
            return false;
        }
        Gryo(x,y,z,w);
        return true;
    }

    public boolean requestPinchZoom(int delta) {
        Header header = Header.newInstance(ControllerContext.CMD_PINCH_ZOOM_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[4];
        ByteUtils.intToByte(delta, buff, 0);
        sendRequest(header, buff);
        return true;
    }
}
