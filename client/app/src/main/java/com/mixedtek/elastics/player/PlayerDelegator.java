package com.mixedtek.elastics.player;

import com.mixedtek.elastics.player.decoder.VideoDecoder;
import com.mixedtek.elastics.player.game.ControllerService;
import com.mixedtek.elastics.player.net.ControlClient;
import com.mixedtek.elastics.player.net.ControlClient.OnControlListener;
import com.mixedtek.elastics.player.net.StreamingClient;
import com.mixedtek.elastics.player.net.StreamingClient.OnStreamingListener;
import com.mixedtek.elastics.player.decoder.AudioDecoder;
import com.mixedtek.elastics.player.decoder.AudioDecoder.OnAudioDecoderListener;
import com.mixedtek.elastics.player.decoder.VideoDecoder.OnVideoDecoderListener;
import com.mixedtek.elastics.player.decoder.AACDecoder;
import com.mixedtek.elastics.player.decoder.OPUSDecoder;
import com.mixedtek.elastics.player.decoder.AVCDecoder;

import android.util.Log;
import android.view.Surface;

public class PlayerDelegator {

    private static final String TAG = "PlayerDelegator";

    /** connection state **/
    public static final int STATE_DISCONNECT = 0;
    public static final int STATE_CONTROL_CONNECTING = 1;
    public static final int STATE_CONTROL_CONNECTED = 2;
    public static final int STATE_STREAM_CONNECTING = 3;
    public static final int STATE_STREAM_PLAYING =4;

    private ControlClient mControlClient;
    private StreamingClient mAudioClient;
    private StreamingClient mVideoClient;

    private Surface mSurface = null;
    private AudioDecoder mAudioDecoder;
    private VideoDecoder mVideoDecoder;

    private int mStreamingPort;
    private String mContainerUUID;

    private int mVideoDisplayWidth;
    private int mVideoDisplayHeight;

    private int mVideoCodec;
    private int mVideoCodecBitrate;
    private int mVideoCodecWidth;
    private int mVideoCodecHeight;
    private int mVideoCodecKeyframeinterval;
    private int mVideoCodecRatecontrol;
    private int mAudioCodec;
    private int mAudioCodecBitrate;
    private int mAudioCodecOption;
    private int mAudioCodecObjectType;
    private int mAudioChannels;
    private int mAudioBitdepth;
    private int mAudioSamplerate;

    private ControllerService mControllerService = null;
    private int mState = STATE_DISCONNECT;
    private OnStateChangeListener mStateChangeListener;
    private OnUpdateResolutionListener mUpdateResolutionListener;

    public PlayerDelegator() {
        mControllerService = null;
    }
    public PlayerDelegator(ControllerService svc) {
        mControllerService = svc;
    }

    public interface OnStateChangeListener {
        public void onStateChanged(int state);
    }

    public interface OnUpdateResolutionListener {
        public void onResolutionUpdated(int videoDisplayWidth, int videoDisplayHeight, int videoCodecWidth, int videoCodecHeight);
    }

    public void setStateChangeListener(OnStateChangeListener listener) {
        mStateChangeListener = listener;
    }

    public void setUpdateResolutionListener(OnUpdateResolutionListener listener) {
        mUpdateResolutionListener = listener;
    }

    public void setSurface(Surface surface) {
        mSurface = surface;
    }

    private void setState(int state) {
        mState = state;

        Log.i(TAG, "setState : " + stateToString(state));

        if(mStateChangeListener != null) {
            mStateChangeListener.onStateChanged(state);
        }
    }

    public int getState() {
        return mState;
    }

    public int getVideoFrameCount() {
        if(mState == STATE_STREAM_PLAYING) return mVideoClient.getFrameCount();

        return 0;
    }

    public void start() {
        if(mState != STATE_DISCONNECT) {
            Log.w(TAG, "Client already started");
            return;
        }
        setState(STATE_CONTROL_CONNECTING);
        connectControlClient(PlayerContext.ServerAddress, PlayerContext.ServerPortnumber);
    }

    public void preStop() {
        if(mVideoDecoder !=null) {
            mVideoDecoder.preStop();
        }
        if(mAudioDecoder!=null) {
            mAudioDecoder.preStop();
        }
    }

    public void stop() {
        if(mState == STATE_DISCONNECT) {
            Log.i(TAG, "Client already stopped");
            return;
        }
        setState(STATE_DISCONNECT);
        stopClient();
        stopDecoder();
        if(mControllerService!=null)
            mControllerService.onDisconnectedContainer();
        mUpdateResolutionListener.onResolutionUpdated(-1, -1, -1, -1);
    }

    public boolean sendKeyDownEvent(int keyCode) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        return mControlClient.requestKeyDown(keyCode);
    }

    public boolean sendKeyUpEvent(int keyCode) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        return mControlClient.requestKeyUp(keyCode);
    }

    public boolean sendMouseDownEvent(int x, int y) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        return mControlClient.requestMouseDown(x, y);
    }

    public boolean sendMouseMoveEvent(int x, int y) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        return mControlClient.requestMouseMove(x, y);
    }

    public boolean sendMouseUpEvent(int x, int y) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        return mControlClient.requestMouseUp(x, y);
    }

    public boolean sendGyroRotEvent(float x, float y, float z, float w) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        return mControlClient.requestGyroRot(x, y, z, w);
    }

    public boolean sendPinchZoomEvent(int delta) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        return mControlClient.requestPinchZoom(delta);
    }

    private void connectControlClient(final String ip, final int port) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                boolean connected;
                if(mControlClient == null) {
                    mControlClient = new ControlClient();
                    mControlClient.setOnControlListener(mControlListener);
                }
                connected = mControlClient.startClient(ip, port, 1024 * 4, 1024 * 4, PlayerContext.CONNECT_TIMEOUT);
                if(connected) {
                    mControlClient.requestCreateSession();
                } else {
                    Log.e(TAG, "connect error");
                    stop();
                }
            }
        });
        thread.start();
    }

    private void connectStreamingClient() {
        boolean connect = false;
        setState(STATE_STREAM_CONNECTING);
        if(connect = connectVideoClient()) {
            connect = connectAudioClient();
        }
        if(connect) {
            setState(STATE_STREAM_PLAYING);
        } else {
            stop();
        }
    }

    private boolean connectVideoClient() {
        boolean isConnect;
        if(mVideoClient!=null) {
            mVideoClient.stopClient();
        }
        mVideoClient = new StreamingClient(mVideoStreamingListener, mContainerUUID, true);
        isConnect = mVideoClient.startClient(PlayerContext.ServerAddress, mStreamingPort, 1024 * 1024 * 2, 1024 * 1024 * 2, PlayerContext.CONNECT_TIMEOUT);
        if(isConnect) {
            mVideoClient.requestCreateSession();
        } else {
            Log.e(TAG, "video client connect error");
        }
        return isConnect;
    }

    private boolean connectAudioClient() {
        if(PlayerContext.AudioEnabled) {
            boolean isConnect;
            if(mAudioClient!=null) {
                mAudioClient.stopClient();
            }
            mAudioClient = new StreamingClient(mAudioStreamingListener, mContainerUUID, false);
            isConnect = mAudioClient.startClient(PlayerContext.ServerAddress, mStreamingPort, 1024 * 1024 * 2, 1024 * 1024 * 2, PlayerContext.CONNECT_TIMEOUT);
            if (isConnect) {
                mAudioClient.requestCreateSession();
            } else {
                Log.e(TAG, "audio client connect error");
            }
            return isConnect;
        } else {
            mAudioClient = null;
            return true;
        }
    }

    private void stopClient() {
        if(mAudioClient != null) {
            mAudioClient.stopClient();
            mAudioClient = null;
        }
        if(mVideoClient != null) {
            mVideoClient.stopClient();
            mVideoClient = null;
        }
        if(mControlClient != null) {
            if(mControlClient.isRun()) {
                mControlClient.requestDisconnectClient();
            }
            mControlClient.stopClient();
            mControlClient = null;
        }
    }

    private void startDecoder() {
        try {
            if(PlayerContext.AudioEnabled) {
                if(mAudioCodec==9) {
                    mAudioDecoder = new AACDecoder(mAudioDecodeListener, mAudioChannels, mAudioBitdepth, mAudioSamplerate, PlayerContext.AudioBufferCapacity, PlayerContext.AudioBufferSize);
                } else {
                    mAudioDecoder = new OPUSDecoder(mAudioDecodeListener, mAudioChannels, mAudioBitdepth, mAudioSamplerate, PlayerContext.AudioBufferCapacity, PlayerContext.AudioBufferSize);
                }
            } else {
                mAudioDecoder = null;
            }
            mVideoDecoder = new AVCDecoder(mSurface, mVideoDecodeListener, mVideoCodecWidth, mVideoCodecHeight, PlayerContext.VideoBufferCapacity, PlayerContext.VideoBufferSize);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if(mAudioDecoder!=null)
            mAudioDecoder.start();
        mVideoDecoder.start();
    }

    private void stopDecoder() {
        Log.e(TAG, "stopDecoder");
        if(mAudioDecoder != null) {
            mAudioDecoder.stop();
            mAudioDecoder = null;
        }
        if(mVideoDecoder != null) {
            mVideoDecoder.stop();
            mVideoDecoder = null;
        }
    }

    public OnControlListener mControlListener = new OnControlListener() {
        @Override
        public void onContainerOpen(int port, String uuid, int videoDisplayWidth, int videoDisplayHeight,
                                    int videoCodec, int videoCodecBitrate, int videoCodecWidth, int videoCodecHeight, int videoCodecKeyframeinterval, int videoCodecRatecontrol,
                                    int audioCodec, int audioCodecBitrate, int audioCodecOption, int audioCodecObjectType, int audioChannels, int audioBitdepth, int audioSamplerate) {
            mStreamingPort = port;
            mContainerUUID = uuid;
            mVideoDisplayWidth = videoDisplayWidth;
            mVideoDisplayHeight = videoDisplayHeight;
            mVideoCodec = videoCodec;
            mVideoCodecBitrate = videoCodecBitrate;
            mVideoCodecWidth = videoCodecWidth;
            mVideoCodecHeight = videoCodecHeight;
            mVideoCodecKeyframeinterval = videoCodecKeyframeinterval;
            mVideoCodecRatecontrol = videoCodecRatecontrol;
            mAudioCodec = audioCodec;
            mAudioCodecBitrate = audioCodecBitrate;
            mAudioCodecOption = audioCodecOption;
            mAudioCodecObjectType = audioCodecObjectType;
            mAudioChannels = audioChannels;
            mAudioBitdepth = audioBitdepth;
            mAudioSamplerate = audioSamplerate;
            Log.e(TAG, "onContainerOpen --------- streaming port: " + String.valueOf(port));

            if(mControllerService!=null) {
                mControllerService.onConnectedContainer(PlayerContext.ServerAddress, PlayerContext.ServerPortnumber, uuid);
            }

            setState(STATE_CONTROL_CONNECTED);
            mUpdateResolutionListener.onResolutionUpdated(mVideoDisplayWidth, mVideoDisplayHeight, mVideoCodecWidth, mVideoCodecHeight);
            startDecoder();

            connectStreamingClient();
        }

        @Override
        public void onContainerClose() {
            Log.e(TAG, "onContainerClose");
            /*
            if(mControllerService!=null)
                mControllerService.onDisconnectedContainer();
            */
            stop();
        }

        @Override
        public void onError(int error) {
            Log.e(TAG, "ControlClient onError : " + PlayerContext.errorCodeToString(error));
            /*
            if(mControllerService!=null)
                mControllerService.onDisconnectedContainer();
            */
            stop();
        }

        @Override
        public void onEnd2End(String infoXml) {
            // 임시 처리. 규격 확장시 처리 방법 수정 필요
            if(infoXml.contains("StopVOD")) {
                stop();
            }
        }
    };

    private OnStreamingListener mAudioStreamingListener = new OnStreamingListener() {
        @Override
        public void onReceiveBitstream(byte[] buffer, int offset, int length, long ts) {
            if(mAudioDecoder!=null)
                mAudioDecoder.decode(buffer, offset, length, ts);
        }

        @Override
        public void onCommandError(short command) {
            Log.e(TAG, "AudioStreaming onCommandError : " + command);
            //stop();
        }

        @Override
        public void onError(int error) {
            Log.e(TAG, "AudioStreaming onError : " + PlayerContext.errorCodeToString(error));
            //stop();
        }
    };

    private OnStreamingListener mVideoStreamingListener = new OnStreamingListener() {
        @Override
        public void onReceiveBitstream(byte[] buffer, int offset, int length, long ts) {
            if(mVideoDecoder !=null) {
                mVideoDecoder.decode(buffer, offset, length, ts);
            }
        }
        @Override
        public void onCommandError(short command) {
            Log.e(TAG, "VideoStreaming onCommandError : " + command);
            //stop();
        }

        @Override
        public void onError(int error) {
            Log.e(TAG, "VideoStreaming onError : " + PlayerContext.errorCodeToString(error));
            stop();
        }
    };

    private OnAudioDecoderListener mAudioDecodeListener = new OnAudioDecoderListener() {
        @Override
        public void onError(int error) {
            Log.e(TAG, "VideoDecoder onError : " + PlayerContext.errorCodeToString(error));
            //stop();
        }
    };

    private OnVideoDecoderListener mVideoDecodeListener = new OnVideoDecoderListener() {
        @Override
        public void onError(int error) {
            Log.e(TAG, "VideoDecoder onError : " + PlayerContext.errorCodeToString(error));
            //stop();
        }
    };

    public static String stateToString(int state) {
        switch(state) {
            case STATE_DISCONNECT:
                return "STATE_DISCONNECT";
            case STATE_CONTROL_CONNECTING:
                return "STATE_CONTROL_CONNECTING";
            case STATE_CONTROL_CONNECTED:
                return "STATE_CONTROL_CONNECTED";
            case STATE_STREAM_CONNECTING:
                return "STATE_STREAM_CONNECTING";
            case STATE_STREAM_PLAYING:
                return "STATE_STREAM_PLAYING";
        }

        return "STATE_NONE";
    }
}
