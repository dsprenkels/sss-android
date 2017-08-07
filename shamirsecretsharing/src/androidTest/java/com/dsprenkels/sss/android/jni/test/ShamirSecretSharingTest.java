package com.dsprenkels.sss.android.jni.test;

import android.support.test.runner.AndroidJUnit4;

import com.dsprenkels.sss.android.jni.ShamirSecretSharing;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;

@RunWith(AndroidJUnit4.class)
public class ShamirSecretSharingTest {
    private final byte[] data;
    private final byte[] key;

    public ShamirSecretSharingTest() {
        data = new byte[64];
        key = new byte[32];
        Arrays.fill(data, (byte) 42);
        Arrays.fill(key, (byte) 42);
    }

    @Test
    public void One_equals_one(){
        ArrayList<byte[]> shares;
        byte[] restored;

        shares = new ArrayList<>(Arrays.asList(ShamirSecretSharing.createShares(data, 5, 4)));
        restored = ShamirSecretSharing.combineShares(shares.toArray(new byte[][] {}));
        assertArrayEquals(restored, data);
        shares.remove(0);
        restored = ShamirSecretSharing.combineShares(shares.toArray(new byte[][] {}));
        assertArrayEquals(restored, data);
        shares.remove(0);
        restored = ShamirSecretSharing.combineShares(shares.toArray(new byte[][] {}));
        assertArrayEquals(restored, null);
        shares.remove(0);
        restored = ShamirSecretSharing.combineShares(shares.toArray(new byte[][] {}));
        assertArrayEquals(restored, null);
        shares.remove(0);
        restored = ShamirSecretSharing.combineShares(shares.toArray(new byte[][] {}));
        assertArrayEquals(restored, null);
//        shares.remove(0);
//        restored = ShamirSecretSharing.combineShares(shares.toArray(new byte[][] {}));
//        assertArrayEquals(restored, data);
    }
}
