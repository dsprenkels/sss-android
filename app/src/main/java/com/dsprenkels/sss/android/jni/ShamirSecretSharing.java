package com.dsprenkels.sss.android.jni;

public final class ShamirSecretSharing {
    public static final int DATA_LEN = 64;
    public static final int SHARE_LEN = 113;
    public static final int KEY_LEN = 32;
    public static final int KEYSHARE_LEN = 33;

    public static native byte[][] createShares(byte[] data, int count, int threshold);
    public static native byte[] combineShares(byte[][] shares);
    public static native byte[][] createKeyshares(byte[] data, int count, int threshold);
    public static native byte[] combineKeyshares(byte[][] shares);

    static {
        System.loadLibrary("sss-jni");
    }
}
