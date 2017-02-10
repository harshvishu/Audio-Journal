package com.brotherpowers.waveformview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

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
            if (max < 0) {
                max = 0;
            }
            if (min > 0) {
                min = 0;
            }
            newData[i] = new short[]{max, min};
        }

        return newData;
    }

    public static int calculateAudioLength(int samplesCount, int sampleRate, int channelCount) {
        return ((samplesCount / channelCount) * 1000) / sampleRate;
    }
}
