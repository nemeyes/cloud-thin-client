package com.mixedtek.elastics.player.net;

import org.json.JSONException;
import org.json.JSONObject;

import com.mixedtek.elastics.player.PlayerContext;
import com.mixedtek.elastics.player.data.Device;
import com.mixedtek.elastics.player.net.base.LightweightClient;
import com.mixedtek.elastics.player.net.data.Header;
import com.mixedtek.elastics.player.utils.ByteUtils;
import com.mixedtek.elastics.player.utils.PlayerTimer;
import com.mixedtek.elastics.player.utils.TimerManager.OnTimeoutListener;

public class ControlClient extends LightweightClient {

    private static final String TAG = "ControlClient";
    private OnControlListener	mControlListener;

    public interface OnControlListener {
        public void onContainerOpen(int port, String uuid, int videoDisplayWidth, int videoDisplayHeight,
                                        int videoCodec, int videoCodecBitrate, int videoCodecWidth, int videoCodecHeight, int videoCodecKeyframeinterval, int videoCodecRatecontrol,
                                        int audioCodec, int audioCodecBitrate, int audioCodecOption, int audioCodecObjectType, int audioChannels, int audioBitdepth, int audioSamplerate);
        public void onContainerClose();
        public void onError(int error);
        public void onEnd2End(String infoXml);
    };

    public void setOnControlListener(OnControlListener listener) {
        mControlListener = listener;
    }

    @Override
    public boolean stopClient() {
        PlayerTimer.stopControlContainerOpenTimer();
        PlayerTimer.stopControlKeepAliveTimer();
        PlayerTimer.stopControlKeepAliveResponseTimer();
        PlayerTimer.stopControlGyroTimer();

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
//						mControlListener.onError(PlayerContext.ERROR_CONNECTION_CLOSE);
                        continue;
                    }

                    command = header.getCommand();
                    bodySize = header.getLength();
                    if(readBody(bodySize) < 0) {
                        mControlListener.onError(PlayerContext.ERROR_CONNECTION_CLOSE);
                        continue;
                    }

                    switch(command) {
                        case PlayerContext.CMD_CREATE_SESSION_RESPONSE: // 1002
                            rcode = getCreateSessionResponseCode(mBody);
                            if(rcode == 0) {
                                byteToSrcUUID(mBody, 4);
                                requestConnectClient();
                            } else {
                                openContainerError();
                            }
                            break;

                        case PlayerContext.CMD_CONNECT_CLIENT_RES: // 2102
                            try {
                                body = new JSONObject(new String(mBody));
                                rcode = body.getInt("rcode");
                                //String rmsg = body.getString("msg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(rcode == 0) {
                            } else {
                                openContainerError();
                            }
                            break;

                        case PlayerContext.CMD_CONTAINER_INFO_IND: // 2104
                            try {
                                body = new JSONObject(new String(mBody));
                                rcode = body.getInt("rcode");

                                if(rcode == 0) {
                                    String containerUUID = body.getString("uuid");
                                    int streamerPortnumber = body.getInt("portnumber");
                                    int videoDisplayWidth = body.getInt("video_display_width");
                                    int videoDisplayHeight = body.getInt("video_display_height");
                                    int videoCodec = body.getInt("video_codec");
                                    int videoCodecBitrate = body.getInt("video_codec_bitrate");
                                    int videoCodecWidth = body.getInt("video_codec_width");
                                    int videoCodecHeight = body.getInt("video_codec_height");
                                    int videoCodecKeyframeinterval = body.getInt("video_codec_keyframeinterval");
                                    int videoCodecRatecontrol = body.getInt("video_codec_ratecontrol");
                                    int audioCodec = body.getInt("audio_codec");
                                    int audioCodecBitrate = body.getInt("audio_codec_bitrate");
                                    int audioCodecOption = body.getInt("audio_codec_option");
                                    int audioCodecObjectType = body.getInt("audio_codec_object_type");
                                    int audioChannels = body.getInt("audio_channels");
                                    int audioBitdepth = body.getInt("audio_bitdepth");
                                    int audioSamplerate = body.getInt("audio_samplerate");

                                    setDestUUID(containerUUID);
                                    PlayerTimer.stopControlContainerOpenTimer();
                                    PlayerTimer.startControlKeepAliveTimer(mTimeoutListener);
                                    mControlListener.onContainerOpen(streamerPortnumber, containerUUID, videoDisplayWidth, videoDisplayHeight,
                                                                        videoCodec, videoCodecBitrate, videoCodecWidth, videoCodecHeight, videoCodecKeyframeinterval, videoCodecRatecontrol,
                                                                        audioCodec, audioCodecBitrate, audioCodecOption, audioCodecObjectType, audioChannels, audioBitdepth, audioSamplerate);
                                } else {
                                    openContainerError();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;

                        case PlayerContext.CMD_KEEPALIVE_RESPONSE: // 1005
                            PlayerTimer.startControlKeepAliveTimer(mTimeoutListener);
                            PlayerTimer.stopControlKeepAliveResponseTimer();
                            break;

                        case PlayerContext.CMD_DISCONNECT_CLIENT_RES: // 2106
                            closeContainer();
                            break;

                        case PlayerContext.CMD_END2END_DATA_IND: // 2202
                            mControlListener.onEnd2End(getString(mBody));
                            break;
                    };
                }
            }
        });
        thread.start();
    }

    private OnTimeoutListener mTimeoutListener = new OnTimeoutListener() {
        public void onTimeout(String type) {
            if(type.equals(PlayerTimer.CONTROL_KEEP_ALIVE)) {
                requestKeepalive();
            } else if(type.equals(PlayerTimer.CONTROL_KEEP_ALIVE_RESPONSE)) {
                keepAliveResponseTimeout();
            } else if(type.equals(PlayerTimer.CONTROL_CONTAINER_OPEN)) {
                openContainerError();
            }
        }
    };

    private void closeContainer() {
        mControlListener.onContainerClose();
    }

    private void keepAliveResponseTimeout() {
        PlayerTimer.stopControlKeepAliveTimer();
        mControlListener.onError(PlayerContext.ERROR_KEEP_ALIVE_FAIL);
    }

    private void openContainerError() {
        PlayerTimer.stopControlContainerOpenTimer();
        mControlListener.onError(PlayerContext.ERROR_CONTAINER_OPEN_FAIL);
    }

    public void requestCreateSession() {
        Header header = Header.newInstance(PlayerContext.CMD_CREATE_SESSION_REQUEST);
        header.setInitialSrcUUID();
        header.setInitialDestUUID();

        sendRequest(header);
        PlayerTimer.startControlContainerOpenTimer(mTimeoutListener);
    }

    public void requestConnectClient() {
        Header header = Header.newInstance(PlayerContext.CMD_CONNECT_CLIENT_REQ);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID();

        JSONObject json = new JSONObject();
        try {
            if(PlayerContext.OnDemandConnection) {
                Device device = Device.getInstance();

                json.put("app_id", PlayerContext.AppID);
                json.put("client_id", device.ClientId);
                json.put("client_width", device.ClientWidth);
                json.put("client_height", device.ClientHeight);
                json.put("client_type", device.ClientType); // mobile
                json.put("client_environment_type", device.ClientEnvironmentType); //android
            } else {
                Device device = Device.getInstance();
                json.put("id", device.ClientId);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        sendRequest(header, json.toString());
    }

    public void requestDisconnectClient() {
        Header header = Header.newInstance(PlayerContext.CMD_DISCONNECT_CLIENT_REQ);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID(); // uuid값 확인
        sendRequest(header);
    }

    public void requestKeepalive() {
        PlayerTimer.startControlKeepAliveResponseTimer(mTimeoutListener);
        Header header = Header.newInstance(PlayerContext.CMD_KEEPALIVE_REQUEST);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID();	// uuid값 확인
        sendRequest(header);
    }

    public boolean requestKeyDown(int keyCode) {
        Header header = Header.newInstance(PlayerContext.CMD_KEY_DOWN_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        keyCode = PlayerContext.convertServerKeyCode(keyCode);
        if(keyCode == -1)
            return false;
        ByteUtils.intToByte(keyCode, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestKeyUp(int keyCode) {
        Header header = Header.newInstance(PlayerContext.CMD_KEY_UP_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        keyCode = PlayerContext.convertServerKeyCode(keyCode);
        if(keyCode == -1)
            return false;
        ByteUtils.intToByte(keyCode, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseDown(int x, int y) {
        Header header = Header.newInstance(PlayerContext.CMD_MOUSE_LBD_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseMove(int x, int y) {
        Header header = Header.newInstance(PlayerContext.CMD_MOUSE_MOVE_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseUp(int x, int y) {
        Header header = Header.newInstance(PlayerContext.CMD_MOUSE_LBU_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestGyro(float x, float y, float z) {
        Header header = Header.newInstance(PlayerContext.CMD_GYRO_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[12];
        ByteUtils.floatToByte(x, buff, 0);
        ByteUtils.floatToByte(y, buff, 4);
        ByteUtils.floatToByte(z, buff, 8);
        sendRequest(header, buff);
        return true;
    }

    public void Gryo(float x, float y, float z, float w)
    {
        Header header = Header.newInstance(PlayerContext.CMD_GYRO_ROT_IND);
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
        if(PlayerContext.CONTROL_KEEP_CONTROL_GYRO_TIME != 0) {
            if(!PlayerTimer.isControlGyro()) {
                Gryo(x,y,z,w);
                PlayerTimer.startControlGyroTimer(mTimeoutListener);
                return true;
            }
            return false;
        }
        Gryo(x,y,z,w);
        return true;
    }

    public boolean requestPinchZoom(int delta) {
        Header header = Header.newInstance(PlayerContext.CMD_PINCH_ZOOM_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[4];
        ByteUtils.intToByte(delta, buff, 0);
        sendRequest(header, buff);
        return true;
    }
}
