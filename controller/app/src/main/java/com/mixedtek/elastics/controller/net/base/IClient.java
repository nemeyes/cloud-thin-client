package com.mixedtek.elastics.controller.net.base;


import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Vector;

import com.mixedtek.elastics.controller.utils.ByteUtils;

import android.os.SystemClock;
import android.util.Log;

public abstract class IClient {

    private static final String     TAG = "IClient";
    private static final int        SENDQ_SIZE = 30;

    protected Socket            mSocket;
    protected DataInputStream   mInputStream;
    protected DataOutputStream  mOutputStream;
    private Thread               mOutThread = null;
    protected boolean          mIsRun = false;
    private Object               mLock = new Object();
    private Vector<byte[]>      mSndQueue = new Vector(SENDQ_SIZE);

    abstract public void startReaderThread();

    protected boolean startClient(final String ip, final int port, final int sndBufferSize, final int rcvBufferSize, final int timeout) {
        try {
            Log.i(TAG, "startClient ip : " + ip + " port : " + port);
            mSocket = new Socket();
            InetSocketAddress address = new InetSocketAddress(ip, port);

            //mSocket.setSoTimeout(5000);
            mSocket.setSoLinger(true, 0);
            mSocket.setSendBufferSize(sndBufferSize);
            mSocket.setReceiveBufferSize(rcvBufferSize);
            mSocket.setTcpNoDelay(true);

            mSocket.connect(address, timeout);

            mInputStream = new DataInputStream(mSocket.getInputStream());
            mOutputStream = new DataOutputStream(mSocket.getOutputStream());

        } catch (Exception e) {
            e.printStackTrace();
            stopClient();
            return false;
        }

        mIsRun = true;
        startSenderThread();
        startReaderThread();
        return true;
    }

    public boolean stopClient() {
        mIsRun = false;
        try {
            stopSenderThread();

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

    public int sendBytes(ByteArrayOutputStream baos) {
        byte buffer[] = null;

        try {
            buffer = baos.toByteArray();
            baos.reset();
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }

        return sendBytes(buffer);
    }

    public int sendBytes(byte buffer[]) {
        synchronized (mLock) {
            if(mSndQueue.size()>=SENDQ_SIZE) {
                mSndQueue.removeAllElements();
            }
            mSndQueue.add(buffer);
            mLock.notify();
        }
        return 0;
    }

    public void startSenderThread() {
        mOutThread = new Thread(new Runnable () {
            @Override
            public void run() {
                while (mIsRun) {
                    synchronized (mLock) {
                        try {
                            for(int index=0; index<mSndQueue.size(); index++) {
                                byte[] buffer = mSndQueue.get(index);
                                mOutputStream.write(buffer, 0, buffer.length);
                                mOutputStream.flush();
                            }

                            mSndQueue.removeAllElements();
                            try {
                                mLock.wait();
                            } catch(Exception e) { }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        mOutThread.setName("Packet Sender Thread");
        //mOutThread.setPriority(Thread.NORM_PRIORITY + 1);
        mOutThread.start();
    }

    public void stopSenderThread() {
        if(mOutThread!=null) {
            try{
                synchronized (mLock) {
                    mLock.notify();
                }
                mOutThread.join();
            } catch(Exception e) {

            } finally {
                mOutThread = null;
            }
        }
    }

    public boolean isRun() {
        return mIsRun;
    }

    public int read(byte[] buffer, int offset, int size) {
        int len = 0;
        while (size > 0) {
            try {
                mInputStream.readFully(buffer, offset, size);
            } catch (SocketTimeoutException e) {
                //Log.i(TAG, "SocketTimeout");
                continue;
            } catch (Exception e) {
                return -2;
            }
            len = size;
            size = 0;
        }

        return len;
    }

    public int read(byte[] buffer, int size) {
        return read(buffer, 0, size);
    }

    public int readInt() {
        byte buffer[] = new byte[4];
        int len = read(buffer, 4);

        if (len < 0) {
            return len;
        }

        if (len == 4) {
            return ByteUtils.byteToInt(buffer);
        }

        return -4;
    }

    public String readString(int size) {
        byte buffer[] = new byte[size];
        int len = read(buffer, size);

        if (len < 0 || len != size) {
            return null;
        }

        return new String(buffer);
    }

    public int appendStreamString(ByteArrayOutputStream baos, String text) {
        if (text == null || text.length() == 0) {
            return appendStreamInt(baos, 0);
        }

        int length = -1;
        try {
            byte[] byBuffer = text.getBytes();
            baos.write(byBuffer);
            length = byBuffer.length;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return length;
    }

    public int appendStreamBytes(ByteArrayOutputStream baos, byte[] buffer) {
        if (buffer == null || buffer.length == 0) {
            return appendStreamInt(baos, 0);
        }

        try {
            baos.write(buffer);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return buffer.length + 4;
    }

    public int appendStreamInt(ByteArrayOutputStream baos, int value) {
        if (baos == null) {
            return 0;
        }

        baos.write((byte) ((value >> 24) & 0xFF));
        baos.write((byte) ((value >> 16) & 0xFF));
        baos.write((byte) ((value >> 8) & 0xFF));
        baos.write((byte) (value & 0xFF));

        return 4;
    }

    /*

     */




}
