package com.brotherpowers.audiojournal.Utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Build;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by harsh_v on 10/27/16.
 */

public final class Extensions {

    public static final SimpleDateFormat formatHumanReadable = new SimpleDateFormat("EEE, d MMM yyyy hh:mm aaa", Locale.ENGLISH);

    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    /**
     * A safe way to get an instance of the CameraFragment object.
     */
    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a CameraFragment instance
        } catch (Exception e) {
            // CameraFragment is not available (in use or does not exist)
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

    public static int adjustAlpha(int color, float factor, float min, float max) {
        if (factor < min) {
            factor = min;
        } else if (factor > max) {
            factor = max;
        }

        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        int alpha = Math.round(Color.alpha(color) * factor);

        return Color.argb(alpha, red, green, blue);
    }

    public static String millisToHMS(long length) {
        String s;
        int hours = (int) ((length / (1000 * 60 * 60)) % 24);
        int min = (int) ((length / (1000 * 60)) % 60);
        int sec = (int) (length / 1000) % 100;
        if (hours == 0) {
            s = String.format(Locale.getDefault(), "%2d Min, %2d Sec", min, sec);
        } else {
            s = String.format(Locale.getDefault(), "%2d Hrs, %2d Min", hours, min);
        }
        return s;
    }

    public static String millisToMSm(float millis) {
        int min = (int) (millis / (1000 * 60)) % 60;
        int sec = (int) (millis / 1000) % 60;
        int mil = (int) (millis % 100);

        return String.format(Locale.getDefault(), "%02d:%02d:%02d", min, sec, mil);
    }

    public static String millisToMS(float millis) {
        int min = (int) (millis / (1000 * 60)) % 60;
        int sec = (int) (millis / 1000) % 60;
        if (min > 0 && sec > 0) {
            return String.format(Locale.getDefault(), "%2d Min, %2d Sec", min, sec);
        } else if (min > 0 && sec == 0) {
            return String.format(Locale.getDefault(), "%2d Min", min);
        }
        return String.format(Locale.getDefault(), "%2d Sec", sec);
    }

    /**
     * Delete the file from disk
     */
    public static AtomicInteger deleteCount = new AtomicInteger(0);

    public static boolean delete(File file) {
        deleteCount.incrementAndGet();
        System.out.println(">>> DELETE COUNT " + deleteCount);
        //noinspection ResultOfMethodCallIgnored
        return file != null && file.delete();
    }
}
