package com.brotherpowers.audiojournal.Reminder;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.media.session.MediaControllerCompat;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Date;

/**
 * Created by harsh_v on 11/29/16.
 */
public class Alarm {
    private static final String TAG = "ALARM";

    private Alarm() {
        // Private constructor to avoid conflict
    }

    /**
     * @param context {@link Context}
     * @param id      {@link com.brotherpowers.audiojournal.Realm.DataEntry}
     */
    public static void set(Context context, long id) {

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, Reminder.class);
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("id", id);
        Gson gson = new Gson();


        intent.putExtra("data", gson.toJson(jsonObject));

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);


        long time = System.currentTimeMillis() + (1000 * 2);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlarmManager.AlarmClockInfo alarmClockInfo = new AlarmManager.AlarmClockInfo(time, pendingIntent);
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent);

            Log.v(TAG, ".... api 21 ...");

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, time, pendingIntent);
            Log.v(TAG, ".... api 23 ...");
        } else {
            alarmManager.set(AlarmManager.RTC, time, pendingIntent);

            Log.v(TAG, ".... api all ...");
        }


        Log.v(TAG, "Alarm set : " + new Date(time).toString() + "current time: " + new Date(System.currentTimeMillis()).toString());
    }


}
