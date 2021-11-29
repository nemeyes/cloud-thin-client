package ai.sibylla.egp.client.game;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Vector;

public class ControllerClient {
    private static final String TAG = "ControllerClient";
    private static final int SENDQ_SIZE = 30;
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

    private boolean mConnected = false;
    private String mServerAddress = null;
    private int mServerPortnumber = -1;
    private String mServerContainerUUID = null;
    public void onConnectedContainer(String address, int portnumber, String uuid) {
        if(!mConnected) {
            String msg = null;
            try {
                JSONObject payload = new JSONObject();
                payload.put("address", address);
                payload.put("portnumber", portnumber);
                payload.put("uuid", uuid);

                JSONObject header = new JSONObject();
                header.put("onConnected", payload);
                msg = header.toString();
            } catch(org.json.JSONException e) {}
            sendMessage(msg);
            mServerAddress = address;
            mServerPortnumber = portnumber;
            mServerContainerUUID = uuid;
            mConnected = true;
        }
    }

    public void onDisconnectedContainer() {
        if(mConnected) {
            String msg = null;
            try {
                JSONObject payload = new JSONObject();
                payload.put("address", mServerAddress);
                payload.put("portnumber", mServerPortnumber);
                payload.put("uuid", mServerContainerUUID);

                JSONObject header = new JSONObject();
                header.put("onDisconnected", payload);
                msg = header.toString();
            } catch(org.json.JSONException e) {}
            sendMessage(msg);
            mConnected = false;
        }
    }

    public boolean startClient(Socket socket) {
        try {
            mSocket = socket;
            //mSocket.setSoTimeout(timeout);
            mSocket.setSoLinger(true, 0);
            mSocket.setTcpNoDelay(true);

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
        //startReaderThread();
        return true;
    }

    public boolean stopClient() {
        try {
            stopSenderThread();
            //stopReaderThread();

            if(mSocket != null) {
                mSocket.close();
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
                        for(int index=0; index<mSndQueue.size(); index++) {
                            String msg = mSndQueue.get(index);
                            mWriter.println(msg);
                            mWriter.flush();
                        }
                        mSndQueue.removeAllElements();

                        try {
                            mSendLock.wait();
                        } catch(InterruptedException e) { }
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

    /*
    public void startReaderThread() {
        mIsRunRecving = true;
        mInThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while(mIsRunRecving) {
                    try {
                        JSONObject json = new JSONObject(mReader.readLine());

                    } catch(IOException ioe) {
                        ioe.printStackTrace();
                    } catch(org.json.JSONException jsne) {
                        jsne.printStackTrace();
                    }
                }
            }
        });
        mInThread.setName("Controller Receiver Thread");
        //mInThread.start();
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
    */

    public boolean isRun() {
        return mIsRunSending/* && mIsRunRecving*/;
    }
}
