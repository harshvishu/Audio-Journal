package com.brotherpowers.audiojournal.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by harsh_v on 10/27/16.
 */

public final class Extensions {

    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    /**
     * This tells whether a service is running or not
     *
     * @param context      current context
     * @param serviceClass Api class
     * @return true if a service is running
     */
    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static String convertDateToServerFormat(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyyy-mm-dd hh:mm:ss", Locale.ENGLISH);
        return format.format(date);
    }

    public static final SimpleDateFormat formatHumanReadable = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa", Locale.ENGLISH);

    public static int getMaxSampleRate(Context context) {
        android.media.AudioManager am = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            String value = am.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException e) {
                return 44100;
            }
        }
        return 44100;
    }
}
