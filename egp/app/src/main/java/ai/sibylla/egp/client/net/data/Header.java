package ai.sibylla.egp.client.net.data;

import ai.sibylla.egp.client.utils.ByteUtils;
import ai.sibylla.egp.client.utils.LogUtils;
import android.util.Log;

public class Header {
    public static final String TAG = "Header";
    public static final byte PID = 'E';
    public static final byte VERSION = 0;

    public static final int SIZE = 40;
    private byte mBuff[] = new byte[SIZE];

    public static Header newInstance(short command) {
        Header header = new Header();
        header.setCommand(command);

        return header;
    }

    public static Header newInstance() {
        Header header = new Header();

        return header;
    }

    public Header() {
        mBuff[0] = PID;
        mBuff[33] = VERSION;
        setLength(0);
    }

    public byte getPID() {
        return mBuff[0];
    }

    public byte[] getDstUUID() {
        byte uuid[] = new byte[16];
        System.arraycopy(mBuff, 1, uuid, 0, uuid.length);
        return uuid;
    }

    public byte[] getSrcUUID() {
        byte uuid[] = new byte[16];
        System.arraycopy(mBuff, 17, uuid, 0, uuid.length);
        return uuid;
    }

    public byte getVersion() {
        return mBuff[33];
    }

    public short getCommand() {
        return ByteUtils.byteToShort(mBuff, 34);
    }

    public int getLength() {

        return ByteUtils.byteToInt(mBuff, 36);
    }

    public int getBodySize()
    {
        return ByteUtils.byteToInt(mBuff, 36);
    }

    public byte[] getByteBuffer()
    {
        return mBuff;
    }

    public void setPID(byte pid)
    {
        mBuff[0] = pid;
    }

    public void setInitialDestUUID() {
        for(int i = 1; i < 17; i++) {
            mBuff[i] = (byte)0x00;
        }
    }

    public void setInitialSrcUUID() {
        for(int i = 17; i < 33; i++) {
            if(i == 32)
                mBuff[i] = (byte)0xfe;
            else
                mBuff[i] = (byte)0xff;
        }

    }

    public void setDstUUID(byte uuid[]) {
        if(uuid.length != 16) {
            Log.e(TAG, "Dst UUID size error size = " + uuid.length);
        }
        System.arraycopy(uuid, 0, mBuff, 1, uuid.length);
    }

    public void setSrcUUID(byte uuid[]) {
        if(uuid.length != 16) {
            Log.e(TAG, "Src UUID size error size = " + uuid.length);
        }
        System.arraycopy(uuid, 0, mBuff, 17, uuid.length);
    }

    public void setVersion(byte version) {

        mBuff[33] = version;
    }

    public void setCommand(short command) {

        setValue(34, Short.valueOf(command));
    }

	/*
	public void setPayload(byte payload) {
		mBuff[3] = payload;
	}
	*/

    public void setLength(int length) {
        setValue(36, Integer.valueOf(length));
    }

    private void setValue(int offset, Object value) {
        if(value instanceof Short) {
            short shortValue = ((Short)value).shortValue();

            /*
            mBuff[offset] = (byte) (shortValue & 0xFF);
            mBuff[offset + 1] = (byte) (shortValue >> 8 & 0xFF);
            */

            mBuff[offset] = (byte) (shortValue >> 8 & 0xFF);
            mBuff[offset + 1] = (byte) (shortValue & 0xFF);

        } else if(value instanceof Integer) {
            int intValue = ((Integer)value).intValue();

            /*
            mBuff[offset] = (byte) (intValue & 0xFF);
            mBuff[offset + 1] = (byte) (intValue >> 8 & 0xFF);
            mBuff[offset + 2] = (byte) (intValue >> 16 & 0xFF);
            mBuff[offset + 3] = (byte) (intValue >> 24 & 0xFF);
            */

            mBuff[offset] = (byte) (intValue >> 24 & 0xFF);
            mBuff[offset + 1] = (byte) (intValue >> 16 & 0xFF);
            mBuff[offset + 2] = (byte) (intValue >> 8 & 0xFF);
            mBuff[offset + 3] = (byte) (intValue & 0xFF);
        }
    }

    public void logBytes() {
        LogUtils.logBytes(TAG, "header", mBuff, mBuff.length);
    }
}

