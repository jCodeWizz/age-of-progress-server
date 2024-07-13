package dev.codewizz.main.utils;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class ByteUtils {

    public static byte[] toBytes(int value, int length) {
        byte[] result = new byte[length];
        for (int i = 0; i < length; i++) {
            result[i] = (byte) (value >> ((length - 1) * 8 - i * 8));
        }
        return result;
    }

    public static byte[] toBytes(int value) {
        byte[] result = new byte[4];
        for (int i = 0; i < 4; i++) {
            result[i] = (byte) (value >> ((4 - 1) * 8 - i * 8));
        }
        return result;
    }

    public static byte[] toBytes(float value) {
        int intValue = Float.floatToIntBits(value);
        return toBytes(intValue, Float.BYTES);
    }

    public static byte toByte(byte b, boolean flag, int pos) {
        int bits = (int) b;
        int bytes = 1 << (7 - pos);
        if (flag) {
            bits = bits | bytes;
        } else {
            int magic = ~bytes;
            bits = bits & magic;
        }
        b = (byte) bits;
        return b;
    }

    public static byte[] toBytes(String value) {
        int length = Math.min(255, value.length());

        byte[] result = new byte[length + 1];
        byte[] data = value.getBytes(StandardCharsets.US_ASCII);

        result[0] = toBytes(value.length(), 1)[0];
        System.arraycopy(data, 0, result, 1, length);

        return result;
    }

    public static byte[] toBytes(double value) {
        long longBits = Double.doubleToLongBits(value);
        byte[] bytes = new byte[8];
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (longBits >> (8 * (7 - i)) & 0xFF);
        }
        return bytes;
    }

    public static byte[] toBytes(UUID uuid) {
        byte[] bytes = new byte[16];
        long mostSigBits = uuid.getMostSignificantBits();
        long leastSigBits = uuid.getLeastSignificantBits();
        for (int i = 0; i < 8; i++) {
            bytes[i] = (byte) (mostSigBits >>> (8 * (7 - i)));
            bytes[8 + i] = (byte) (leastSigBits >>> (8 * (7 - i)));
        }
        return bytes;
    }

    public static boolean toBoolean(byte b, int pos) {
        int checker = 1 << (7 - pos);
        return (b & checker) != 0;
    }

    public static String toString(byte[] data, int index) {
        int length = toInteger(data, index, 1);

        byte[] stringData = new byte[length];

        System.arraycopy(data, index + 1, stringData, 0, length);

        return new String(stringData, StandardCharsets.US_ASCII);
    }

    public static float toFloat(byte[] bytes) {
        int intValue = toInteger(bytes);
        return Float.intBitsToFloat(intValue);
    }

    public static int toInteger(byte[] bytes) {
        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= (bytes[i] & 0xFF) << (8 * (bytes.length - 1 - i));
        }
        return result;
    }

    public static int toInteger(byte[] data, int index, int length) {
        byte[] a = new byte[length];
        System.arraycopy(data, index, a, 0, length);
        return toInteger(a);
    }

    public static int toInteger(byte[] data, int index) {
        byte[] a = new byte[Integer.BYTES];
        System.arraycopy(data, index, a, 0, Integer.BYTES);
        return toInteger(a);
    }

    public static float toFloat(byte[] data, int index) {
        byte[] a = new byte[Float.BYTES];
        System.arraycopy(data, index, a, 0, Float.BYTES);
        return toFloat(a);
    }

    public static double toDouble(byte[] data, int index) {
        byte[] a = new byte[Double.BYTES];
        System.arraycopy(data, index, a, 0, Double.BYTES);
        return toDouble(a);
    }

    public static double toDouble(byte[] bytes) {
        long longBits = 0;
        for (int i = 0; i < 8; i++) {
            longBits |= ((long) bytes[i] & 0xFF) << (8 * (7 - i));
        }

        return Double.longBitsToDouble(longBits);
    }

    public static UUID toUUID(byte[] data) {
        long mostSigBits = 0;
        long leastSigBits = 0;
        for (int i = 0; i < 8; i++) {
            mostSigBits = (mostSigBits << 8) | (data[i] & 0xff);
            leastSigBits = (leastSigBits << 8) | (data[8 + i] & 0xff);
        }
        return new UUID(mostSigBits, leastSigBits);
    }

    public static UUID toUUID(byte[] data, int index) {
        byte[] a = new byte[16];
        System.arraycopy(data, index, a, 0, 16);
        return toUUID(a);
    }
}
