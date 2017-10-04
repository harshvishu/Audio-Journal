package com.brotherpowers.audiojournal.Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by harsh_v on 7/4/17.
 */

public class Blur {
    public static Bitmap blurActivity(Activity context){
        final View root = context.getWindow().getDecorView().getRootView();
        root.setDrawingCacheEnabled(true);


        return null;
    }
}
