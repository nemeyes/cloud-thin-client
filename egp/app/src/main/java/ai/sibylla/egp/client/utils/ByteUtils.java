package ai.sibylla.egp.client.utils;

public class ByteUtils {

    public static short byteToShort(byte[] src, int offset) {
        int s1 = src[offset] & 0xFF;
        int s2 = src[offset + 1] & 0xFF;

        return (short)((s1 << 8) + s2);
    }

    public static int byteToInt(byte[] src, int offset) {
        int s1 = src[offset] & 0xFF;
        int s2 = src[offset + 1] & 0xFF;
        int s3 = src[offset + 2] & 0xFF;
        int s4 = src[offset + 3] & 0xFF;

        return ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
    }

    public static int byteToInt(byte[] buffer) {
        if (buffer == null || buffer.length < 4) {
            return 0;
        }
        return ((buffer[0] & 0xFF) << 24) + ((buffer[1] & 0xFF) << 16) + ((buffer[2] & 0xFF) << 8) + ((buffer[3] & 0xFF));
    }

    public static long byteToLong(byte[] src, int offset) {
        long s1 = src[offset] & 0xFFL;
        long s2 = src[offset + 1] & 0xFFL;
        long s3 = src[offset + 2] & 0xFFL;
        long s4 = src[offset + 3] & 0xFFL;
        long s5 = src[offset + 4] & 0xFFL;
        long s6 = src[offset + 5] & 0xFFL;
        long s7 = src[offset + 6] & 0xFFL;
        long s8 = src[offset + 7] & 0xFFL;

        return ((s1 << 56) + (s2 << 48) + (s3 << 40) + (s4 << 32) + (s5 << 24) + (s6 << 16) + (s7 << 8) + (s8 << 0));
    }

    public static void intToByte(int value, byte[] dest, int offset) {
        dest[offset] = (byte)(value >> 24 & 0xFF);
        dest[offset + 1] = (byte)(value >> 16 & 0xFF);
        dest[offset + 2] = (byte)(value >> 8 & 0xFF);
        dest[offset + 3] = (byte)(value & 0xFF);
    }

    public static void floatToByte(float value, byte[] dest, int offset) {
        int intValue = Float.floatToIntBits(value);

        dest[offset] = (byte)(intValue >> 24 & 0xFF);
        dest[offset + 1] = (byte)(intValue >> 16 & 0xFF);
        dest[offset + 2] = (byte)(intValue >> 8 & 0xFF);
        dest[offset + 3] = (byte)(intValue & 0xFF);
    }
}
