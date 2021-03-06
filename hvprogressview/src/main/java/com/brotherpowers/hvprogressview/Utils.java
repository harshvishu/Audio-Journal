package com.brotherpowers.hvprogressview;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.TypedValue;

/**
 * Created by harsh_v on 11/22/16.
 */

final class Utils {
    static float getFontSize(Context ctx, int textAppearance) {
        TypedValue typedValue = new TypedValue();
        ctx.getTheme().resolveAttribute(textAppearance, typedValue, true);
        int[] textSizeAttr = new int[]{android.R.attr.textSize};
        TypedArray arr = ctx.obtainStyledAttributes(typedValue.data, textSizeAttr);
        float fontSize = arr.getDimensionPixelSize(0, -1);
        arr.recycle();
        return fontSize;
    }

    interface Defaults {
        int START_ANGLE = -90;
        int TEXT_OFFSET = 64;
        String TEXT_START_TIME = "00:00:00";
        String TEXT_HEADER = "HH:MM:SS";
    }
}
