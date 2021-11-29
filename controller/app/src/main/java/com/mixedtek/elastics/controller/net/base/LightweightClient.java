package com.mixedtek.elastics.controller.net.base;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

import com.mixedtek.elastics.controller.net.data.Header;
import com.mixedtek.elastics.controller.utils.ByteUtils;

public abstract class LightweightClient extends IClient {

    private static final String    TAG = "LightWeightClient";
    protected byte                  mSrcUUID[] = new byte[16];
    protected byte                  mDestUUID[] = new byte[16];
    protected byte                  mBody[] = new byte[1024];

    protected Header readHeader() {
        Header header = Header.newInstance();
        byte[] buff = header.getByteBuffer();
        int ret = read(buff, Header.SIZE);
        if(ret < 0)
            return null;
        return header;
    }

    protected boolean readHeader(Header header) {
        byte[] buff = header.getByteBuffer();
        int ret = read(buff, Header.SIZE);
        return ret > 0;
    }

    protected int readBody(int size) {
        if (mBody.length < size) {
            int resize = ((size / 1024) + 1) * 1024;
            mBody = new byte[resize];
        }
        return read(mBody, size);
    }

    protected int getCreateSessionResponseCode(byte buff[]) {
        return ByteUtils.byteToInt(buff);
    }

    protected String getString(byte buff[]) {
        String converted = "";
        try {
            converted = new String(buff, "UTF-8");
        }catch (UnsupportedEncodingException ex) {

        }
        return converted;
    }

    // UUID 응답 mSrcUUID로 변환(45 38 41 46 : E8AF ==> mSrcUUID[0] : E8, [1] : AF)
    protected void byteToSrcUUID(byte buff[], int offset) {
        String hexStr = "";
        for (int i = offset; i < buff.length; i++) {
            if (buff[i] == 0) break;

            if (buff[i] == '-') {
                continue;
            }

            hexStr += (char) buff[i];
        }

        for (int i = 0, j = 0; i < hexStr.length() && j < mSrcUUID.length; i = i + 2) {
            mSrcUUID[j] = (byte) Integer.parseInt(hexStr.substring(i, i + 2), 16);
            j++;
        }
    }

    protected void setDestUUID(byte destUUID[]) {
        System.arraycopy(mSrcUUID, 0, mDestUUID, 0, mDestUUID.length);
    }

    protected void setDestUUID(String uuid) {
        uuid = uuid.replace("-", "");
        for (int i = 0, j = 0; i < uuid.length() && j < mDestUUID.length; i = i + 2) {
            mDestUUID[j] = (byte) Integer.parseInt(uuid.substring(i, i + 2), 16);
            j++;
        }
    }

    // header만 전송
    protected void sendRequest(Header header) {
        sendRequest(header, "");
    }

    // header body를 묶어서 전송
    protected void sendRequest(Header header, String body) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        header.setLength((body == null ? 0 : body.length()));
        //header.logBytes();

        appendStreamBytes(baos, header.getByteBuffer());
        if (body != null && !body.isEmpty()) {
            appendStreamString(baos, body);
        }

        sendBytes(baos);
    }

    // header body를 묶어서 전송
    protected void sendRequest(Header header, byte[] body) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        header.setLength(body.length);
        //header.logBytes();

        appendStreamBytes(baos, header.getByteBuffer());
        appendStreamBytes(baos, body);
        sendBytes(baos);
    }
}