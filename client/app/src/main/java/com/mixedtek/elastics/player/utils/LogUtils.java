package com.mixedtek.elastics.player.utils;

import java.io.ByteArrayOutputStream;
import android.util.Log;

public class LogUtils {

    public static void logBytes(String tag, byte[] buff, int size) {
        logBytes(tag, null, buff, size);
    }

    public static void logBytes(String  tag, String prefix, byte[] buff, int size) {
        StringBuffer buffer = new StringBuffer();

        for(int i = 0; i < size; i++) {
            buffer.append(String.format("%02x ", buff[i]));
        }

        if(prefix == null || prefix.isEmpty()) {
            Log.i(tag, "bytes : " + buffer.toString());
        } else {
            Log.i(tag, prefix + " : " + buffer.toString());
        }
    }

    public static void logByteStream(String tag, ByteArrayOutputStream baos) {
        logByteStream(tag, null, baos);
    }

    public static void logByteStream(String tag, String prefix, ByteArrayOutputStream baos) {
        byte buff[] = baos.toByteArray();
        logBytes(tag, prefix, buff, buff.length);
    }


    public static String longToHexString(long value) {
        StringBuffer buffer = new StringBuffer();

        buffer.append("0x");
        for(int i = 7; i >= 0; i--) {
            buffer.append(String.format("%02X", (value >> 8 * i) & 0xFF));
        }

        return buffer.toString();
    }
}
