package com.mixedtek.elastics.controller.net;

import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Vector;

public class PlayerClient {
    private static final String     TAG = "PlayerClient";
    private static final int        SENDQ_SIZE = 30;
    private Socket mSocket = null;

    private DataInputStream mInputStream = null;
    BufferedReader mReader = null;
    private Thread mInThread = null;
    private boolean mIsRunRecving = false;
    private Object mRecvLock = new Object();

    private DataOutputStream mOutputStream = null;
    private PrintWriter mWriter = null;
    private Thread mOutThread = null;
    private boolean mIsRunSending = false;
    private Object mSendLock = new Object();
    private Vector<String> mSndQueue = new Vector(SENDQ_SIZE);

    private OnPlayerClientListener mControllerListener;

    public interface OnPlayerClientListener {
        public void onConnected(String address, int portnumber, String uuid);
        public void onDisconnected(String address, int portnumber, String uuid);
    }

    public void setOnControllerListener(OnPlayerClientListener listener) {
        mControllerListener = listener;
    }

    public boolean startClient(final String ip, final int port) {
        try {
            Log.i(TAG, "startClient ip : " + ip + " port : " + port);
            mSocket = new Socket();
            InetSocketAddress address = new InetSocketAddress(ip, port);
            //mSocket.setSoTimeout(timeout);
            mSocket.setSoLinger(true, 0);
            //mSocket.setSendBufferSize(sndBufferSize);
            //mSocket.setReceiveBufferSize(rcvBufferSize);
            mSocket.setTcpNoDelay(true);

            mSocket.connect(address, 3000);

            mInputStream = new DataInputStream(mSocket.getInputStream());
            mReader = new BufferedReader(new InputStreamReader(mInputStream));

            mOutputStream = new DataOutputStream(mSocket.getOutputStream());
            mWriter = new PrintWriter(mOutputStream);

        } catch (Exception e) {
            e.printStackTrace();
            stopClient();
            return false;
        }

        startSenderThread();
        startReaderThread();
        return true;
    }

    public boolean stopClient() {
        try {
            if(mSocket != null) {
                mSocket.close();
            }
            stopSenderThread();
            stopReaderThread();

            if(mSocket != null) {
                mSocket = null;
            }

            if(mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }

            if(mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public int sendMessage(String msg) {
        synchronized (mSendLock) {
            if(mSndQueue.size()>=SENDQ_SIZE) {
                mSndQueue.removeAllElements();
            }
            mSndQueue.add(msg);
            mSendLock.notify();
        }
        return 0;
    }

    public void startSenderThread() {
        mIsRunSending = true;
        mOutThread = new Thread(new Runnable () {
            @Override
            public void run() {
                while (mIsRunSending) {
                    synchronized (mSendLock) {
                        try {
                            mSendLock.wait();
                        } catch(InterruptedException e) { }

                        for(int index=0; index<mSndQueue.size(); index++) {
                            String msg = mSndQueue.get(index);
                            mWriter.println(msg);
                            mWriter.flush();
                        }
                        mSndQueue.removeAllElements();
                    }
                }
            }
        });
        mOutThread.setName("Controller Sender Thread");
        mOutThread.start();
    }

    public void stopSenderThread() {
        mIsRunSending = false;
        if(mOutThread!=null) {
            try{
                synchronized (mSendLock) {
                    mSendLock.notify();
                }
                mOutThread.join();
            } catch(Exception e) {

            } finally {
                mOutThread = null;
                mSndQueue.clear();
            }
        }
    }

    public void startReaderThread() {
        mIsRunRecving = true;
        mInThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mIsRunRecving) {
                    try {
                        JSONObject header = new JSONObject(mReader.readLine());
                        if(header.has("onConnected") && !header.isNull("onConnected")) {
                            JSONObject payload = header.getJSONObject("onConnected");
                            String address = payload.getString("address");
                            int portnumber = payload.getInt("portnumber");
                            String uuid = payload.getString("uuid");
                            if(address!=null && uuid!=null && portnumber>0) {
                                if(mControllerListener!=null) {
                                    mControllerListener.onConnected(address, portnumber, uuid);
                                }
                            }
                        } else if(header.has("onDisconnected") && !header.isNull("onDisconnected")) {
                            JSONObject payload = header.getJSONObject("onDisconnected");
                            String address = payload.getString("address");
                            int portnumber = payload.getInt("portnumber");
                            String uuid = payload.getString("uuid");
                            if(address!=null && uuid!=null && portnumber>0) {
                                if(mControllerListener!=null) {
                                    mControllerListener.onDisconnected(address, portnumber, uuid);
                                }
                            }
                        }


                    } catch(IOException ioe) {
                        if(!mIsRunRecving)
                            break;
                        ioe.printStackTrace();
                    } catch(org.json.JSONException jsne) {
                        jsne.printStackTrace();
                    }

                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {}
                }
            }
        });
        mInThread.setName("Controller Receiver Thread");
        mInThread.start();
    }

    public void stopReaderThread() {
        mIsRunRecving = false;
        if(mInThread!=null) {
            try{
                synchronized (mRecvLock) {
                    mRecvLock.notify();
                }
                mInThread.join();
            } catch(Exception e) {

            } finally {
                mInThread = null;
            }
        }
    }

    public boolean isRun() {
        return mIsRunSending && mIsRunRecving;
    }
}
