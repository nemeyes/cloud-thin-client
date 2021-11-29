package ai.sibylla.egp.client.net;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import ai.sibylla.egp.client.ClientContext;
import ai.sibylla.egp.client.data.Device;
import ai.sibylla.egp.client.net.base.LightweightClient;
import ai.sibylla.egp.client.net.data.Header;
import ai.sibylla.egp.client.utils.ByteUtils;
import ai.sibylla.egp.client.utils.ClientTimer;
import ai.sibylla.egp.client.utils.TimerManager.OnTimeoutListener;

public class ControlClient extends LightweightClient {

    private static final String TAG = "ControlClient";
    private OnControlListener	mControlListener;

    public interface OnControlListener {
        public void onContainerOpen(int port, String uuid, int videoDisplayWidth, int videoDisplayHeight,
                                        int videoCodec, int videoCodecBitrate, int videoCodecWidth, int videoCodecHeight, int videoCodecKeyframeinterval, int videoCodecRatecontrol,
                                        int audioCodecBitrate, int audioChannels, int audioBitdepth, int audioSamplerate);
        public void onContainerClose();
        public void onError(int error);
        public void onEnd2End(String infoXml);
    };

    public void setOnControlListener(OnControlListener listener) {
        mControlListener = listener;
    }

    @Override
    public boolean stopClient() {
        ClientTimer.stopControlContainerOpenTimer();
        ClientTimer.stopControlKeepAliveTimer();
        ClientTimer.stopControlKeepAliveResponseTimer();
        ClientTimer.stopControlGyroTimer();

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
//						mControlListener.onError(ClientContext.ERROR_CONNECTION_CLOSE);
                        continue;
                    }

                    command = header.getCommand();
                    bodySize = header.getLength();
                    if(readBody(bodySize) < 0) {
                        mControlListener.onError(ClientContext.ERROR_CONNECTION_CLOSE);
                        continue;
                    }

                    switch(command) {
                        case ClientContext.CMD_CREATE_SESSION_RESPONSE: // 1002
                            rcode = getCreateSessionResponseCode(mBody);
                            if(rcode == 0) {
                                byteToSrcUUID(mBody, 4);
                                requestConnectClient();
                                ClientTimer.startControlKeepAliveTimer(mTimeoutListener);
                            } else {
                                Log.e(TAG, "CMD_CREATE_SESSION_RESPONSE rcode with failed");
                                openContainerError();
                            }
                            break;

                        case ClientContext.CMD_DESTROY_SESSION_INDICATION :
                            closeContainer();
                            break;

                        case ClientContext.CMD_CONNECT_CLIENT_RES: // 2102
                            try {
                                body = new JSONObject(new String(mBody));
                                rcode = body.getInt("rcode");
                                //String rmsg = body.getString("msg");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if(rcode != 0) {
                                Log.e(TAG, "CMD_CONNECT_CLIENT_RES rcode with failed");
                                openContainerError();
                            }
                            break;

                        case ClientContext.CMD_CONTAINER_INFO_IND: // 2104
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

                                    int audioCodecBitrate = body.getInt("audio_codec_bitrate");
                                    int audioChannels = body.getInt("audio_channels");
                                    int audioBitdepth = body.getInt("audio_bitdepth");
                                    int audioSamplerate = body.getInt("audio_samplerate");

                                    setDestUUID(containerUUID);
                                    ClientTimer.stopControlContainerOpenTimer();
                                    //ClientTimer.startControlKeepAliveTimer(mTimeoutListener);
                                    mControlListener.onContainerOpen(streamerPortnumber, containerUUID, videoDisplayWidth, videoDisplayHeight,
                                                                        videoCodec, videoCodecBitrate, videoCodecWidth, videoCodecHeight, videoCodecKeyframeinterval, videoCodecRatecontrol,
                                                                        audioCodecBitrate, audioChannels, audioBitdepth, audioSamplerate);
                                } else {
                                    Log.e(TAG, "CMD_CONTAINER_INFO_IND rcode with failed");
                                    openContainerError();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            break;

                        case ClientContext.CMD_KEEPALIVE_RESPONSE: // 1005
                            ClientTimer.startControlKeepAliveTimer(mTimeoutListener);
                            ClientTimer.stopControlKeepAliveResponseTimer();
                            break;

                        case ClientContext.CMD_DISCONNECT_CLIENT_RES: // 2106
                            closeContainer();
                            break;

                        case ClientContext.CMD_END2END_DATA_IND: // 2202
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
            if(type.equals(ClientTimer.CONTROL_KEEP_ALIVE)) {
                requestKeepalive();
            } else if(type.equals(ClientTimer.CONTROL_KEEP_ALIVE_RESPONSE)) {
                keepAliveResponseTimeout();
            } else if(type.equals(ClientTimer.CONTROL_CONTAINER_OPEN)) {
                Log.e(TAG, "CONTROL_CONTAINER_OPEN Timer Error");
                openContainerError();
            }
        }
    };

    private void closeContainer() {
        mControlListener.onContainerClose();
    }

    private void keepAliveResponseTimeout() {
        ClientTimer.stopControlKeepAliveTimer();
        mControlListener.onError(ClientContext.ERROR_KEEP_ALIVE_FAIL);
    }

    private void openContainerError() {
        ClientTimer.stopControlContainerOpenTimer();
        mControlListener.onError(ClientContext.ERROR_CONTAINER_OPEN_FAIL);
    }

    public void requestCreateSession() {
        Header header = Header.newInstance(ClientContext.CMD_CREATE_SESSION_REQUEST);
        header.setInitialSrcUUID();
        header.setInitialDestUUID();

        sendRequest(header);
        ClientTimer.startControlContainerOpenTimer(mTimeoutListener);
    }

    public void requestConnectClient() {
        Header header = Header.newInstance(ClientContext.CMD_CONNECT_CLIENT_REQ);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID();

        JSONObject json = new JSONObject();
        try {
            Device device = Device.getInstance();

            json.put("app_id", ClientContext.AppID);
            json.put("client_id", device.ClientId);
            json.put("client_width", device.ClientWidth);
            json.put("client_height", device.ClientHeight);
        } catch (JSONException e) {


            e.printStackTrace();
        }
        sendRequest(header, json.toString());
    }

    public void requestDisconnectClient() {
        Header header = Header.newInstance(ClientContext.CMD_DISCONNECT_CLIENT_REQ);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID(); // uuid값 확인
        sendRequest(header);
    }

    public void requestKeepalive() {
        ClientTimer.startControlKeepAliveResponseTimer(mTimeoutListener);
        Header header = Header.newInstance(ClientContext.CMD_KEEPALIVE_REQUEST);
        header.setSrcUUID(mSrcUUID);
        header.setInitialDestUUID();	// uuid값 확인
        sendRequest(header);
    }

    public boolean requestKeyState(byte[] buff) {
        Header header = Header.newInstance(ClientContext.CMD_KEYBOARD_STATE_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseState(byte[] buff) {
        Header header = Header.newInstance(ClientContext.CMD_MOUSE_STATE_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        sendRequest(header, buff);
        return true;
    }

    /*
    public boolean requestKeyDown(int keyCode) {
        Header header = Header.newInstance(ClientContext.CMD_KEY_DOWN_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(keyCode, buff, 4);

        sendRequest(header, buff);
        return true;
    }

    public boolean requestKeyUp(int keyCode) {
        Header header = Header.newInstance(ClientContext.CMD_KEY_UP_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(keyCode, buff, 4);

        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseDown(int x, int y) {
        Header header = Header.newInstance(ClientContext.CMD_MOUSE_LBD_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseMove(int x, int y) {
        Header header = Header.newInstance(ClientContext.CMD_MOUSE_MOVE_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }

    public boolean requestMouseUp(int x, int y) {
        Header header = Header.newInstance(ClientContext.CMD_MOUSE_LBU_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[8];
        ByteUtils.intToByte(x, buff, 0);
        ByteUtils.intToByte(y, buff, 4);
        sendRequest(header, buff);
        return true;
    }
    */

    public boolean requestGyro(float x, float y, float z) {
        Header header = Header.newInstance(ClientContext.CMD_GYRO_IND);
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
        Header header = Header.newInstance(ClientContext.CMD_GYRO_ROT_IND);
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
        if(ClientContext.CONTROL_KEEP_CONTROL_GYRO_TIME != 0) {
            if(!ClientTimer.isControlGyro()) {
                Gryo(x,y,z,w);
                ClientTimer.startControlGyroTimer(mTimeoutListener);
                return true;
            }
            return false;
        }
        Gryo(x,y,z,w);
        return true;
    }

    public boolean requestPinchZoom(int delta) {
        Header header = Header.newInstance(ClientContext.CMD_PINCH_ZOOM_IND);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        byte buff[] = new byte[4];
        ByteUtils.intToByte(delta, buff, 0);
        sendRequest(header, buff);
        return true;
    }
}
