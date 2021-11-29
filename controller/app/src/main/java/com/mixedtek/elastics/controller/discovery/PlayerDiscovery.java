package com.mixedtek.elastics.controller.discovery;

import android.content.Context;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager;
import android.util.Log;

import com.mixedtek.elastics.controller.ControllerDelegator;
import com.mixedtek.elastics.controller.MainActivity;

import java.net.InetAddress;

public class PlayerDiscovery {
    public static final String TAG = "PlayerDiscovery";
    public static final String SERVICE_TYPE = "_egp._tcp";
    public static final String SERVICE_NAME = "EGPController";
    MainActivity mFront = null;
    NsdManager mNsdManager = null;
    NsdManager.ResolveListener mResolveListener = null;
    NsdManager.DiscoveryListener mDiscoveryListener = null;

    private OnDiscoveryEventListener mDiscoveryEventListener;

    public interface OnDiscoveryEventListener {
        public void onServiceResolved(String address, int port);
        public void onServiceResolveFailed();
        public void onServerLost();
    }

    public void setDiscoveryEventListener(OnDiscoveryEventListener listener) {
        mDiscoveryEventListener = listener;
    }

    public void initialize(MainActivity front) {
        mFront = front;
        mNsdManager = (NsdManager) mFront.getSystemService(Context.NSD_SERVICE);
        mResolveListener = new NsdManager.ResolveListener() {
            @Override
            public void onResolveFailed(NsdServiceInfo serviceInfo, int errorCode) {
                Log.e(TAG, "Resolve failed" + errorCode);
                if(mDiscoveryEventListener!=null)
                    mDiscoveryEventListener.onServiceResolveFailed();
            }
            @Override
            public void onServiceResolved(NsdServiceInfo serviceInfo) {
                Log.e(TAG, "Resolve Succeeded. " + serviceInfo);
                if(mDiscoveryEventListener!=null)
                    mDiscoveryEventListener.onServiceResolved(serviceInfo.getHost().getHostAddress(), serviceInfo.getPort());
            }
        };
    }

    public void release() {
        stopDiscovery();
        mNsdManager = null;
    }

    public void startDiscovery() {
        stopDiscovery();  // Cancel any existing discovery request
        initializeDiscoveryListener();
        mNsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
    }

    public void stopDiscovery() {
        if (mDiscoveryListener != null) {
            try {
                mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            } finally {
            }
            mDiscoveryListener = null;
        }
    }

    public void initializeDiscoveryListener() {
        mDiscoveryListener = new NsdManager.DiscoveryListener() {
            @Override
            public void onDiscoveryStarted(String regType) {
                Log.d(TAG, "Service discovery started");
            }
            @Override
            public void onServiceFound(NsdServiceInfo service) {
                /*
                Log.d(TAG, "Service discovery success : " + service.getServiceName());
                Log.d(TAG, "Service discovery success : " + SERVICE_NAME);
                Log.d(TAG, "Service discovery success : " + service.getServiceType());
                Log.d(TAG, "Service discovery success : " + SERVICE_TYPE);
                */
                mNsdManager.resolveService(service, mResolveListener);
            }
            @Override
            public void onServiceLost(NsdServiceInfo service) {
                Log.e(TAG, "service lost" + service);
                if(mDiscoveryEventListener!=null)
                    mDiscoveryEventListener.onServerLost();
            }
            @Override
            public void onDiscoveryStopped(String serviceType) {
                Log.i(TAG, "Discovery stopped: " + serviceType);
            }
            @Override
            public void onStartDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
            @Override
            public void onStopDiscoveryFailed(String serviceType, int errorCode) {
                Log.e(TAG, "Discovery failed: Error code:" + errorCode);
            }
        };
    }
}
