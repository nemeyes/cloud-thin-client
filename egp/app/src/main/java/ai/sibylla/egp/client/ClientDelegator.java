package ai.sibylla.egp.client;

import ai.sibylla.egp.client.decoder.VideoDecoder;
import ai.sibylla.egp.client.game.ControllerService;
import ai.sibylla.egp.client.net.ControlClient;
import ai.sibylla.egp.client.net.ControlClient.OnControlListener;
import ai.sibylla.egp.client.net.StreamingClient;
import ai.sibylla.egp.client.net.StreamingClient.OnStreamingListener;
import ai.sibylla.egp.client.decoder.AudioDecoder;
import ai.sibylla.egp.client.decoder.AudioDecoder.OnAudioDecoderListener;
import ai.sibylla.egp.client.decoder.VideoDecoder.OnVideoDecoderListener;
import ai.sibylla.egp.client.decoder.OPUSDecoder;
import ai.sibylla.egp.client.decoder.AVCDecoder;

import android.os.AsyncTask;
import android.util.Log;
import android.view.Surface;

import java.nio.ByteBuffer;
import java.util.Vector;

public class ClientDelegator {

    private static final String TAG = "ClientDelegator";

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
    private int mAudioCodecBitrate;
    private int mAudioChannels;
    private int mAudioBitdepth;
    private int mAudioSamplerate;

    private ControllerService mControllerService = null;
    private int mState = STATE_DISCONNECT;
    private OnStateChangeListener mStateChangeListener;
    private OnUpdateResolutionListener mUpdateResolutionListener;

    private Object mKeyboardLock = new Object();
    private Object mMouseLock = new Object();
    private Vector<KeyEvent> mKeyboardStates = new Vector<KeyEvent>(256);
    private MouseEvent mMouseState = new MouseEvent();
    private Thread mUserInputThread = null;
    private boolean mbUserInputThread = false;

    class KeyEvent {
        public int code;
        public int state;
    }

    class MouseEvent {
        public int x;
        public int y;
        public int z;
        public boolean [] rgbButtons = new boolean[8];
    }

    public ClientDelegator() {
        mControllerService = null;
    }
    public ClientDelegator(ControllerService svc) {
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
        connectControlClient(ClientContext.ServerAddress, ClientContext.ServerPortnumber);
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

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                stopUserInputThread();
                stopClient();
                stopDecoder();
                if(mControllerService!=null)
                    mControllerService.onDisconnectedContainer();
                mUpdateResolutionListener.onResolutionUpdated(-1, -1, -1, -1);
            }
        });
    }

    public boolean sendKeyDown(int keyCode) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        synchronized (mKeyboardLock) {
            KeyEvent ke = new KeyEvent();
            ke.code = keyCode;
            ke.state |= 0x80;
            mKeyboardStates.add(ke);
        }
        return true;
    }

    public boolean sendKeyUp(int keyCode) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        synchronized (mKeyboardLock) {
            KeyEvent ke = new KeyEvent();
            ke.code = keyCode;
            //ke.state &= ~0x80;
            mKeyboardStates.add(ke);
        }
        return true;
    }

    public boolean sendMouseDown(int type) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        synchronized (mMouseLock) {
            mMouseState.rgbButtons[(int)type] = true;
        }
        return true;
    }

    public boolean sendMouseUp(int type) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        synchronized (mMouseLock) {
            mMouseState.rgbButtons[(int)type] = false;
        }
        return true;
    }

    public boolean sendMouseMove(long x, long y, long z) {
        if(mState != STATE_STREAM_PLAYING)
            return false;

        synchronized (mMouseLock) {
            mMouseState.x += x;
            mMouseState.y += y;
            mMouseState.z += z;
        }
        return true;
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
                connected = mControlClient.startClient(ip, port, 1024 * 4, 1024 * 4, ClientContext.CONNECT_TIMEOUT);
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
        isConnect = mVideoClient.startClient(ClientContext.ServerAddress, mStreamingPort, 1024 * 1024 * 2, 1024 * 1024 * 2, ClientContext.CONNECT_TIMEOUT);
        if(isConnect) {
            mVideoClient.requestCreateSession();
        } else {
            Log.e(TAG, "video client connect error");
        }
        return isConnect;
    }

    private boolean connectAudioClient() {
        if(ClientContext.AudioEnabled) {
            boolean isConnect;
            if(mAudioClient!=null) {
                mAudioClient.stopClient();
            }
            mAudioClient = new StreamingClient(mAudioStreamingListener, mContainerUUID, false);
            isConnect = mAudioClient.startClient(ClientContext.ServerAddress, mStreamingPort, 1024 * 1024 * 2, 1024 * 1024 * 2, ClientContext.CONNECT_TIMEOUT);
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

    private void startUserInputThread() {
        mUserInputThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mbUserInputThread) {
                    //send key input
                    byte[] state = null;
                    synchronized (mKeyboardLock) {
                        if(mKeyboardStates.size()>0) {
                            ByteBuffer buff = ByteBuffer.allocate(mKeyboardStates.size() * 2 * Integer.BYTES);
                            for (KeyEvent keyboardState : mKeyboardStates) {
                                buff.putInt(keyboardState.code);
                                buff.putInt(keyboardState.state);
                            }
                            mKeyboardStates.removeAllElements();
                            state = buff.array();
                        }
                    }
                    if(state!=null) {
                        mControlClient.requestKeyState(state);
                    }

                    //send mouse input
                    synchronized (mMouseLock) {
                        ByteBuffer buff = ByteBuffer.allocate(3 * Integer.BYTES + 8);
                        buff.putInt(mMouseState.x);
                        buff.putInt(mMouseState.y);
                        buff.putInt(mMouseState.z);
                        for(int i=0; i<8; i++) {
                            if(mMouseState.rgbButtons[i]) {
                                buff.put((byte)1);
                            } else {
                                buff.put((byte)0);
                            }
                        }
                        state = buff.array();
                    }
                    if(state!=null) {
                        mControlClient.requestMouseState(state);
                    }

                    try {
                        Thread.sleep(16);
                    } catch(InterruptedException ignored) {}
                }
            }
        });

        mbUserInputThread = true;
        mUserInputThread.start();
    }

    private void stopUserInputThread() {
        mbUserInputThread = false;
        if(mUserInputThread!=null) {
            mUserInputThread.interrupt();
            try {
                mUserInputThread.join();
            } catch (InterruptedException ignored) {
            }
            mUserInputThread = null;
        }
    }

    private void startDecoder() {
        Log.e(TAG, "startDecoder");

        try {
            if(ClientContext.AudioEnabled) {
                mAudioDecoder = new OPUSDecoder(mAudioDecodeListener, mAudioChannels, mAudioBitdepth, mAudioSamplerate, ClientContext.AudioBufferCapacity, ClientContext.AudioBufferSize);
            } else {
                mAudioDecoder = null;
            }
            mVideoDecoder = new AVCDecoder(mSurface, mVideoDecodeListener, mVideoCodecWidth, mVideoCodecHeight, ClientContext.VideoBufferCapacity, ClientContext.VideoBufferSize);
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
                                    int audioCodecBitrate, int audioChannels, int audioBitdepth, int audioSamplerate) {
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
            mAudioCodecBitrate = audioCodecBitrate;
            mAudioChannels = audioChannels;
            mAudioBitdepth = audioBitdepth;
            mAudioSamplerate = audioSamplerate;
            Log.e(TAG, "onContainerOpen --------- streaming port: " + String.valueOf(port));

            if(mControllerService!=null) {
                mControllerService.onConnectedContainer(ClientContext.ServerAddress, ClientContext.ServerPortnumber, uuid);
            }

            startUserInputThread();
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
            Log.e(TAG, "ControlClient onError : " + ClientContext.errorCodeToString(error));
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
            //Log.e(TAG, "AudioStreaming onCommandError : " + command);
            stop();
        }

        @Override
        public void onError(int error) {
            //Log.e(TAG, "AudioStreaming onError : " + ClientContext.errorCodeToString(error));
            stop();
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
            //Log.e(TAG, "VideoStreaming onCommandError : " + command);
            stop();
        }

        @Override
        public void onError(int error) {
            // Log.e(TAG, "VideoStreaming onError : " + ClientContext.errorCodeToString(error));
            stop();
        }
    };

    private OnAudioDecoderListener mAudioDecodeListener = new OnAudioDecoderListener() {
        @Override
        public void onError(int error) {
            //Log.e(TAG, "VideoDecoder onError : " + ClientContext.errorCodeToString(error));
            //stop();
        }
    };

    private OnVideoDecoderListener mVideoDecodeListener = new OnVideoDecoderListener() {
        @Override
        public void onError(int error) {
            //Log.e(TAG, "VideoDecoder onError : " + ClientContext.errorCodeToString(error));
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
