package com.dsprenkels.sss.android.jni;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Arrays;
import java.util.Formatter;

public final class ShamirSecretSharing extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sss_jni);
        TextView tv = (TextView)findViewById(R.id.hello_textview);

        // Do sss stuff
        byte[] tmp = new byte[64];
        tmp[0] = 1;
        tmp[1] = 2;
        tmp[2] = 3;
        tmp[3] = 4;
        tmp[4] = 5;
        byte[][] shares = createShares(tmp, 5, 4);
        byte[][] shares2 = Arrays.copyOfRange(shares, 0, 4);
        byte[] restored = combineShares(shares2);
        tv.setText(byteToHex(restored));
    }

    static public native byte[][] createShares(byte[] data, int count, int threshold);
    static public native byte[] combineShares(byte[][] shares);

    static private String byteToHex(final byte[] bytes)
    {
        Formatter formatter = new Formatter();
        for (byte b : bytes) {
            formatter.format("%02x", b);
        }
        String result = formatter.toString();
        formatter.close();
        return result;
    }

    static {
        System.loadLibrary("sss-jni");
    }
}
