package com.mixedtek.elastics.player.net;

import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import com.mixedtek.elastics.player.PlayerContext;
import com.mixedtek.elastics.player.net.base.LightweightClient;
import com.mixedtek.elastics.player.net.data.Header;
import com.mixedtek.elastics.player.utils.ByteUtils;

import android.os.Environment;
import android.util.Log;

public class StreamingClient extends LightweightClient {

    private static final String TAG = "StreamingClient";
    private OnStreamingListener mStreamingListener;
    private boolean mIsVideo;
    private String mContainerUUID;
    private int mFrameCount = 0;

    public interface OnStreamingListener {
        public void onReceiveBitstream(byte[] buffer, int offset, int length, long ts);
        public void onCommandError(short command);
        public void onError(int error);
    };

    public StreamingClient(OnStreamingListener listener, String containerUUID, boolean isVideo) {
        mStreamingListener = listener;
        mContainerUUID = containerUUID;
        mIsVideo = isVideo;
    }

    @Override
    public void startReaderThread() {
        Thread thread = new Thread(new Runnable() {
            private boolean mIsStream = false;

            @Override
            public void run() {
                while(mIsRun) {
                    if(mIsStream) {
                        Log.e(TAG, "readStream");
                        readStream();
                    } else {
                        Log.e(TAG, "readCommand");
                        mIsStream = readCommand();
                    }
                }
            }
        });
        thread.start();
    }


    private boolean readCommand() {
        Header header = readHeader();
        JSONObject body = null;
        short command;
        int bodySize;
        int rcode;

        if(header == null) {
            mStreamingListener.onError(PlayerContext.ERROR_CONNECTION_CLOSE);
            return false;
        }

        command = header.getCommand();
        bodySize = header.getLength();
        rcode = -1;

        if(readBody(bodySize) < 0) {
            mStreamingListener.onError(PlayerContext.ERROR_CONNECTION_CLOSE);
            return false;
        }

        switch(command) {
            case PlayerContext.CMD_CREATE_SESSION_RESPONSE: // 1002
                rcode = getCreateSessionResponseCode(mBody);
                if(rcode == 0) {
                    byteToSrcUUID(mBody, 4);
                    requestStream();
                } else {
                    // 오류 예외 처리 추가 필요
                }
                break;

            case PlayerContext.CMD_PLAY_RES: // 8012

                try {
                    body = new JSONObject(new String(mBody));
                    rcode = body.getInt("rcode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

				if(mIsVideo && rcode != 0) {
					mStreamingListener.onError(PlayerContext.ERROR_STREAM_REQ_FAIL);
					return false;
				}
                return true;
        }

        return false;
    }

    private void readStream() {
        byte buffer[];
        boolean first = true;
        Header header = Header.newInstance();
        int bodySize, readSize;
        short command;

        while(mIsRun) {
            if(!readHeader(header)) {
                mStreamingListener.onError(PlayerContext.ERROR_CONNECTION_CLOSE);
                continue;
            }

            command = header.getCommand();
            if(command !=1005 && command != 4003 && command != 4004 && command != 4005) {
                mStreamingListener.onCommandError(command);
                //stopClient();
                continue;
            }

            bodySize = header.getBodySize();
            if(bodySize < 0) {
                //Log.e(TAG, "ERROR bodysize error");
//				header.logBytes();
            }

            buffer = new byte[bodySize];
            if(buffer!=null) {
                readSize = read(buffer, bodySize);

                int index = 0;
                short count = 0;
                int length = 0;
                long ts = 0;

                count = ByteUtils.byteToShort(buffer, index);
                index += 2;

                for(int i=0; i < count; i++) {
                    length = ByteUtils.byteToInt(buffer, index);
                    index += 4;
                    ts = ByteUtils.byteToLong(buffer, index);
                    index += 8;
                    mStreamingListener.onReceiveBitstream(buffer, index, length, ts);
                    index += length;
                }
                buffer = null;
            }
/*
            if(mIsVideo) {
				writeVideoFile(buffer); // video es 파일 저장
            } else {
				writeAudioFile(buffer); // audio aac파일 저장
            }
*/
            mFrameCount++;
        }
    }

    public void writeVideoFile(byte[] data) {
        String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/dump.ts";
        FileOutputStream out;
        int length = ByteUtils.byteToInt(data, 13);

        try {
            out = new FileOutputStream(fileName, true);
            out.write(data, 17, length);
            out.flush();
            out.close();
        } catch(Exception e) {

        }
    }

    public void writeAudioFile(byte[] data) {
        String fileName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getPath() + "/dump.aac";
        FileOutputStream out;
        int length = ByteUtils.byteToInt(data, 13);

        try {
            out = new FileOutputStream(fileName, true);
            out.write(data, 17, length);
            out.flush();
            out.close();
        } catch(Exception e) {
        }
    }

    public int getFrameCount() {
        return mFrameCount;
    }

    public void requestCreateSession() {
        Header header = Header.newInstance(PlayerContext.CMD_CREATE_SESSION_REQUEST);
        header.setInitialSrcUUID();
        header.setInitialDestUUID();

        sendRequest(header);
    }

    public void requestStream() {
        Header header = Header.newInstance(PlayerContext.CMD_PLAY_REQ);
        header.setSrcUUID(mSrcUUID);
        header.setDstUUID(mDestUUID);

        JSONObject json = new JSONObject();
        try {
            json.put("type", mIsVideo?1:2);
            //json.put("slotUUID", mContainerUUID);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        sendRequest(header, json.toString());
    }
}
