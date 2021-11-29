package com.mixedtek.elastics.player.game;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ControllerService extends Service {
    private static final String TAG = "ControllerService";
    private NsdManager mNsdManager = null;
    private ServerSocket mServerSocket = null;
    private Thread mServerThread = null;

    private Object mNotificationLock = new Object();
    private ControllerClient mClient = null;
    private NsdManager.RegistrationListener mRegistrationListener = null;

    private IBinder mBinder = new ControllerBinder();

    private boolean mConnected = false;
    private String mServerAddress = null;
    private int mServerPortnumber ;
    private String mServerContainerUUID = null;

    public class ControllerBinder extends Binder {
        public ControllerService getService() {
            return ControllerService.this;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, String.format("onCreate"));
        initializeRegistrationListener();
        Log.i(TAG, String.format("initializeRegistrationListener is completed"));
        mNsdManager = (NsdManager) getSystemService(Context.NSD_SERVICE);
        startServer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        stopServer();
        super.onDestroy();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void onConnectedContainer(String address, int portnumber, String uuid) {
        mServerAddress = address;
        mServerPortnumber = portnumber;
        mServerContainerUUID = uuid;
        if(mClient!=null)
            mClient.onConnectedContainer(address, portnumber, uuid);
        mConnected = true;
    }

    public void onDisconnectedContainer() {
        if(mClient!=null)
            mClient.onDisconnectedContainer();
        mConnected = false;
    }

    public void startServer() {
        Log.i(TAG, String.format("startServer"));
        mServerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    mServerSocket = new ServerSocket(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                int localPort = mServerSocket.getLocalPort();
                Log.i(TAG, "localPort is " + localPort);
                registerService(localPort);
                while(!Thread.currentThread().isInterrupted()) {
                    try {
                        Socket clientSock = mServerSocket.accept();
                        if(mClient!=null) {
                            mClient.stopClient();
                            mClient = null;
                        }
                        mClient = new ControllerClient();
                        mClient.startClient(clientSock);
                        if(mConnected) {
                            mClient.onConnectedContainer(mServerAddress, mServerPortnumber, mServerContainerUUID);
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                }
                if(mClient!=null) {
                    mClient.stopClient();
                    mClient = null;
                }

                unregisterService();
                try {
                    mServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mServerSocket = null;
            }
        });
        mServerThread.start();
        Log.i(TAG, String.format("End of startServer"));
    }

    public void stopServer() {
        try {
            mServerSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mServerThread.interrupt();
        try {
            mServerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mServerSocket = null;
    }

    public void registerService(int port) {
        NsdServiceInfo serviceInfo = new NsdServiceInfo();
        serviceInfo.setServiceName("EGPController");
        serviceInfo.setServiceType("_egp._tcp");
        serviceInfo.setPort(port);
        mNsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);
        Log.i(TAG, "register Service");
    }

    public void unregisterService() {
        mNsdManager.unregisterService(mRegistrationListener);
    }

    public void initializeRegistrationListener() {
        mRegistrationListener = new NsdManager.RegistrationListener() {

            @Override
            public void onServiceRegistered(NsdServiceInfo NsdServiceInfo) {
                Log.i(TAG, String.format("onServiceRegistered"));
            }

            @Override
            public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errCode) {
                Log.i(TAG, String.format("onRegistrationFailed"));
            }

            @Override
            public void onServiceUnregistered(NsdServiceInfo arg0) {
                Log.i(TAG, String.format("onServiceUnregistered"));
            }

            @Override
            public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errCode) {
                Log.i(TAG, String.format("onUnregistrationFailed"));
            }
        };
    }

    /*
    public void notifyServerInfo(String address, int port, String uuid) {
       String msg = null;
       try {
           JSONObject  data = new JSONObject();
           data.put("address", address);
           data.put("portnumber", port);
           data.put("uuid", uuid);

           JSONObject serverInfo = new JSONObject();
           serverInfo.put("server_info", data);
           msg = serverInfo.toString();
       } catch(org.json.JSONException e) {}

       if(msg!=null) {
           if(mClient!=null) {
               mClient.sendMessage(msg);
           } else {
               synchronized (mNotificationLock) {
                   ControllerMessage noti = new ControllerMessage();
                   noti.msg = msg;
                   mNotificationMessages.add(noti);
               }
           }
       }
    }
    */
}
