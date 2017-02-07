package com.brotherpowers.waveformview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * Created by harsh_v on 11/21/16.
 */

public final class Utils {
    static float getFontSize(Context ctx, int textAppearance) {
        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(textAppearance, typedValue, true);
        int[] textSizeAttr = new int[]{android.R.attr.textSize};
        TypedArray arr = ctx.obtainStyledAttributes(typedValue.data, textSizeAttr);
        float fontSize = arr.getDimensionPixelSize(0, -1);
        arr.recycle();
        return fontSize;
    }

    public static short[] getAudioSamples(File audioFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(audioFile, "r");

        int n = 0;
        if (raf.length() > 1024) {
            n = 1024;
            raf.seek(raf.length() - 1024);
        }

        byte[] buffer = new byte[n];
        raf.read(buffer,0,n);

//        DataInputStream dis = new DataInputStream(new FileInputStream(audioFile));
//        dis.readFully(buffer);
        ShortBuffer sb = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer();
        short[] samples = new short[sb.limit()];
        sb.get(samples);
        return samples;
    }

    public static short[][] getExtremes(short[] data, int sampleSize) {

        short[][] newData = new short[sampleSize][];
        int groupSize = data.length / sampleSize;

        for (int i = 0; i < sampleSize; i++) {
            short[] group = Arrays.copyOfRange(data, i * groupSize,
                    Math.min((i + 1) * groupSize, data.length));

            // Fin min & max values
            short min = Short.MAX_VALUE, max = Short.MIN_VALUE;
            for (short a : group) {
                min = (short) Math.min(min, a);
                max = (short) Math.max(max, a);
            }
            newData[i] = new short[]{max, min};
        }

        return newData;
    }
}
