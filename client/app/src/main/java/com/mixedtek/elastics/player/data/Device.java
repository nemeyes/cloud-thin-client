package com.mixedtek.elastics.player.data;

import java.net.NetworkInterface;

import android.app.Activity;
import android.graphics.Point;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;

public class Device {
    public static final String TAG = "Device";
    public String   ClientId;			        // Device ID(셋탑 박스 ID, MAC Address)
    public int  ClientType;		            // 셋탑인지 폰인지 여부("settop", "mobile")
    public int  ClientEnvironmentType;	    // 디바이스 OS("android", "ios", "native")
    public int  ClientWidth;			    // Resolution width("1280");
    public int  ClientHeight;			    // Resolution height("720");

    private static Device mDevice;

    public static void init(Activity activity) {
        if(mDevice == null) {
            mDevice = new Device(activity);
        }
    }

    public static Device getInstance() {
        return mDevice;
    }

    public Device(Activity activity) {
        Point screenSize = new Point();
        ClientId = getMacAddress();

        Display display = activity.getWindowManager().getDefaultDisplay();
        DisplayMetrics metric = new DisplayMetrics();
        display.getSize(screenSize);
        display.getRealMetrics(metric);

        // 디바이스가 추가될때마다 확인이 필요함.
        if(/*metric.density <= 2.0 && */  !Build.MODEL.contains("SM-") || !Build.MODEL.contains("LG-")) {
            ClientType = 0;//"settop";
        } else {
            ClientType = 1;//"mobile";
        }

        ClientEnvironmentType = 0;//"android";
        ClientWidth = screenSize.x;
        ClientHeight = screenSize.y;

        Log.i(TAG, "Density : " + metric.density + " " + Build.MODEL);
        Log.i(TAG, "ScreenSize : " + screenSize.toString());
    }

    public boolean isSetopDevice() {
        return (ClientType == 0);
    }

    public String getMacAddress() {
        StringBuffer buffer = new StringBuffer();
        try {
            byte[] address;
            NetworkInterface netf = NetworkInterface.getByName("eth0"); // 이더넷

            if(netf == null) {
                netf = NetworkInterface.getByName("wlan0"); // wifi
            }

            address = netf.getHardwareAddress();

            for(int i = 0; i < address.length; i++) {
                buffer.append(String.format("%02x", address[i]));
                if(i != address.length - 1) {
                    buffer.append(":");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
